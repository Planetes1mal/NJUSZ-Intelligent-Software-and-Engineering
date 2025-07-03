import cv2
import numpy as np
import glob
import os
import networkx as nx
from collections import defaultdict
from scipy.optimize import least_squares

class MultiImageStitcher:
    def __init__(self):
        # 特征检测器
        self.sift = cv2.SIFT_create(nfeatures=1500, contrastThreshold=0.04, edgeThreshold=12)
        self.orb = cv2.ORB_create(nfeatures=1000)
        
        # 匹配参数
        self.ratio_threshold = 0.8
        self.min_match_count = 10
        self.min_inlier_ratio = 0.15
        
    def preprocess_image(self, img):
        """
        图像预处理
        """
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        
        # 对比度增强
        clahe = cv2.createCLAHE(clipLimit=1.5, tileGridSize=(8,8))
        enhanced = clahe.apply(gray)
        
        return enhanced
        
    def robust_feature_matching(self, img1, img2):
        """
        特征匹配
        """
        gray1 = self.preprocess_image(img1)
        gray2 = self.preprocess_image(img2)
        
        # 首先尝试SIFT
        kp1, des1 = self.sift.detectAndCompute(gray1, None)
        kp2, des2 = self.sift.detectAndCompute(gray2, None)
        
        if des1 is None or des2 is None or len(des1) < 8 or len(des2) < 8:
            # 如果SIFT失败，尝试ORB
            print("SIFT特征不足，尝试ORB...")
            kp1, des1 = self.orb.detectAndCompute(gray1, None)
            kp2, des2 = self.orb.detectAndCompute(gray2, None)
            use_sift = False
        else:
            use_sift = True
            
        if des1 is None or des2 is None or len(des1) < 8 or len(des2) < 8:
            return None, None, 0
        
        # 选择匹配器
        if use_sift:
            FLANN_INDEX_KDTREE = 1
            index_params = dict(algorithm=FLANN_INDEX_KDTREE, trees=5)
            search_params = dict(checks=50)
            matcher = cv2.FlannBasedMatcher(index_params, search_params)
        else:
            matcher = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=False)
        
        # k-NN匹配
        matches = matcher.knnMatch(des1, des2, k=2)
        
        # Lowe's ratio test
        good_matches = []
        for match_pair in matches:
            if len(match_pair) == 2:
                m, n = match_pair
                if m.distance < self.ratio_threshold * n.distance:
                    good_matches.append(m)
        
        if len(good_matches) < self.min_match_count:
            return None, None, len(good_matches)
        
        # 提取匹配点坐标
        src_pts = np.float32([kp1[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
        dst_pts = np.float32([kp2[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)
        
        return src_pts, dst_pts, len(good_matches)
    
    def robust_homography_estimation(self, src_pts, dst_pts):
        """
        改进的单应性矩阵估计
        """
        if src_pts is None or dst_pts is None:
            return None, None
        
        best_H = None
        best_mask = None
        best_inliers = 0
        best_inlier_ratio = 0
        
        # RANSAC参数
        configs = [
            {"threshold": 3.0, "maxIters": 2000, "confidence": 0.99},
            {"threshold": 4.0, "maxIters": 1500, "confidence": 0.95},
            {"threshold": 5.0, "maxIters": 1000, "confidence": 0.9},
        ]
        
        for config in configs:
            for _ in range(2):
                H, mask = cv2.findHomography(
                    src_pts, dst_pts, 
                    cv2.RANSAC, 
                    ransacReprojThreshold=config["threshold"],
                    maxIters=config["maxIters"],
                    confidence=config["confidence"]
                )
                
                if H is not None and mask is not None:
                    inliers = np.sum(mask)
                    inlier_ratio = inliers / len(src_pts)
                    
                    if inlier_ratio > best_inlier_ratio:
                        best_H = H
                        best_mask = mask
                        best_inliers = inliers
                        best_inlier_ratio = inlier_ratio
        
        if best_inlier_ratio < self.min_inlier_ratio:
            return None, None
            
        return best_H, best_mask
    
    def validate_homography(self, H, img_shape, inlier_ratio):
        """
        单应性矩阵验证
        """
        if H is None:
            return False
            
        h, w = img_shape
        corners = np.float32([[0, 0], [w, 0], [w, h], [0, h]]).reshape(-1, 1, 2)
        
        try:
            transformed = cv2.perspectiveTransform(corners, H)
            
            # 1. 面积变化检查
            original_area = w * h
            transformed_area = abs(cv2.contourArea(transformed))
            area_ratio = transformed_area / original_area
            
            if area_ratio < 0.05 or area_ratio > 20:
                return False
            
            # 2. 边长比例检查
            edge_lengths = []
            for i in range(4):
                edge = transformed[(i+1)%4] - transformed[i]
                edge_length = np.linalg.norm(edge[0])
                edge_lengths.append(edge_length)
            
            if len(edge_lengths) > 0 and max(edge_lengths) / min(edge_lengths) > 10:
                return False
            
            # 3. 降低内点比例要求
            if inlier_ratio < 0.1:
                return False
                
            return True
            
        except Exception as e:
            print(f"单应性验证出错: {e}")
            return False
    
    def build_matching_graph(self, images):
        """
        构建图像匹配图
        """
        n = len(images)
        G = nx.Graph()
        
        for i in range(n):
            G.add_node(i)
        
        print(f"构建 {n} 张图像的匹配图...")
        
        # 计算图像对匹配
        match_results = {}
        for i in range(n):
            for j in range(i + 1, n):
                print(f"匹配图像 {i}-{j}...")
                src_pts, dst_pts, match_count = self.robust_feature_matching(images[i], images[j])
                
                if src_pts is not None:
                    H, mask = self.robust_homography_estimation(src_pts, dst_pts)
                    if H is not None:
                        inlier_count = np.sum(mask)
                        inlier_ratio = inlier_count / len(src_pts)
                        
                        # 更宽松的验证
                        if self.validate_homography(H, images[i].shape[:2], inlier_ratio):
                            match_results[(i, j)] = {
                                'homography': H,
                                'src_pts': src_pts[mask.ravel() == 1],
                                'dst_pts': dst_pts[mask.ravel() == 1],
                                'match_count': inlier_count,
                                'inlier_ratio': inlier_ratio
                            }
                            G.add_edge(i, j, weight=inlier_count)
                            print(f"图像 {i}-{j}: {inlier_count} 内点 ({inlier_ratio:.2%})")
                        else:
                            print(f"图像 {i}-{j}: 验证失败 ({inlier_ratio:.2%})")
                    else:
                        print(f"图像 {i}-{j}: 单应性估计失败")
                else:
                    print(f"图像 {i}-{j}: 特征匹配失败")
        
        print(f"构建完成，{len(G.edges)} 条连接")
        return G, match_results
    
    def bundle_adjustment_conservative(self, images, G, match_results, reference):
        """
        束调整
        """
        n = len(images)
        
        # 初始化单应性矩阵
        homographies = {}
        homographies[reference] = np.eye(3, dtype=np.float32)
        
        # 使用BFS计算初始单应性矩阵
        visited = set([reference])
        queue = [reference]
        
        while queue:
            current = queue.pop(0)
            for neighbor in G.neighbors(current):
                if neighbor not in visited:
                    if (current, neighbor) in match_results:
                        H = match_results[(current, neighbor)]['homography']
                        homographies[neighbor] = homographies[current] @ H
                    elif (neighbor, current) in match_results:
                        H = match_results[(neighbor, current)]['homography']
                        try:
                            H_inv = np.linalg.inv(H)
                            homographies[neighbor] = homographies[current] @ H_inv
                        except:
                            continue
                    
                    visited.add(neighbor)
                    queue.append(neighbor)
        
        # 束调整
        def residual_function(params):
            residuals = []
            param_idx = 0
            
            # 重建单应性矩阵
            current_homographies = {reference: np.eye(3, dtype=np.float32)}
            
            for img_id in range(n):
                if img_id != reference and img_id in homographies:
                    H_params = params[param_idx:param_idx+8]
                    H = np.array([
                        [H_params[0], H_params[1], H_params[2]],
                        [H_params[3], H_params[4], H_params[5]],
                        [H_params[6], H_params[7], 1.0]
                    ], dtype=np.float32)
                    current_homographies[img_id] = H
                    param_idx += 8
            
            # 计算重投影误差
            for (i, j), match_info in match_results.items():
                if i in current_homographies and j in current_homographies:
                    H_i_to_ref = current_homographies[i]
                    H_j_to_ref = current_homographies[j]
                    
                    try:
                        H_rel = np.linalg.inv(H_j_to_ref) @ H_i_to_ref
                        src_pts = match_info['src_pts']
                        dst_pts = match_info['dst_pts']
                        
                        reproj_pts = cv2.perspectiveTransform(src_pts, H_rel)
                        diff = reproj_pts - dst_pts
                        residuals.extend(diff.ravel())
                    except:
                        continue
            
            return np.array(residuals)
        
        # 准备初始参数
        initial_params = []
        for img_id in range(n):
            if img_id != reference and img_id in homographies:
                H = homographies[img_id]
                params = [H[0,0], H[0,1], H[0,2], H[1,0], H[1,1], H[1,2], H[2,0], H[2,1]]
                initial_params.extend(params)
        
        if len(initial_params) > 0:
            try:
                # 束调整
                result = least_squares(residual_function, initial_params, 
                                     max_nfev=200, ftol=1e-4)
                
                if result.success:
                    print("束调整优化成功")
                    # 更新单应性矩阵
                    param_idx = 0
                    for img_id in range(n):
                        if img_id != reference and img_id in homographies:
                            H_params = result.x[param_idx:param_idx+8]
                            H = np.array([
                                [H_params[0], H_params[1], H_params[2]],
                                [H_params[3], H_params[4], H_params[5]],
                                [H_params[6], H_params[7], 1.0]
                            ], dtype=np.float32)
                            homographies[img_id] = H
                            param_idx += 8
                else:
                    print("束调整优化失败，使用初始结果")
            except Exception as e:
                print(f"束调整出错: {e}")
        
        return homographies
    
    def compute_canvas_bounds(self, images, homographies):
        """
        计算画布边界
        """
        all_corners = []
        
        for i, img in enumerate(images):
            if i not in homographies:
                continue
                
            h, w = img.shape[:2]
            corners = np.float32([[0, 0], [w, 0], [w, h], [0, h]]).reshape(-1, 1, 2)
            
            H = homographies[i]
            try:
                transformed = cv2.perspectiveTransform(corners, H)
                all_corners.append(transformed)
            except:
                continue
        
        if not all_corners:
            return 2000, 2000, 0, 0
        
        all_corners = np.concatenate(all_corners, axis=0)
        x_coords = all_corners[:, 0, 0]
        y_coords = all_corners[:, 0, 1]
        
        x_min, x_max = np.floor(x_coords.min()).astype(int), np.ceil(x_coords.max()).astype(int)
        y_min, y_max = np.floor(y_coords.min()).astype(int), np.ceil(y_coords.max()).astype(int)
        
        return x_max - x_min, y_max - y_min, x_min, y_min
    
    def improved_blending(self, img1, mask1, img2, mask2):
        """
        改进的图像融合
        """
        # 找到重叠区域
        overlap = (mask1 > 0) & (mask2 > 0)
        
        if not np.any(overlap):
            # 无重叠，直接合并
            result = np.zeros_like(img1)
            result[mask1 > 0] = img1[mask1 > 0]
            result[mask2 > 0] = img2[mask2 > 0]
            return result
        
        # 在重叠区域使用渐变融合
        result = np.zeros_like(img1)
        
        # 非重叠区域直接复制
        only_img1 = (mask1 > 0) & (mask2 == 0)
        only_img2 = (mask2 > 0) & (mask1 == 0)
        result[only_img1] = img1[only_img1]
        result[only_img2] = img2[only_img2]
        
        # 重叠区域使用距离加权
        if np.any(overlap):
            # 计算到边界的距离
            dist1 = cv2.distanceTransform((mask1 > 0).astype(np.uint8), cv2.DIST_L2, 5)
            dist2 = cv2.distanceTransform((mask2 > 0).astype(np.uint8), cv2.DIST_L2, 5)
            
            # 在重叠区域计算权重
            total_dist = dist1[overlap] + dist2[overlap]
            weight1 = dist1[overlap] / (total_dist + 1e-8)
            weight2 = dist2[overlap] / (total_dist + 1e-8)
            
            # 融合
            blended = (img1[overlap].astype(np.float32) * weight1[:, np.newaxis] + 
                      img2[overlap].astype(np.float32) * weight2[:, np.newaxis])
            result[overlap] = np.clip(blended, 0, 255).astype(np.uint8)
        
        return result
    
    def create_mosaic(self, images):
        """
        创建多图像拼接
        """
        if len(images) < 2:
            return images[0] if len(images) == 1 else None
        
        print(f"开始拼接 {len(images)} 张图像...")
        
        # 适度缩放
        max_dimension = 1000
        processed_images = []
        
        for img in images:
            h, w = img.shape[:2]
            if max(h, w) > max_dimension:
                scale = max_dimension / max(h, w)
                new_w, new_h = int(w * scale), int(h * scale)
                img = cv2.resize(img, (new_w, new_h))
            processed_images.append(img)
        
        images = processed_images
        
        # 构建匹配图
        G, match_results = self.build_matching_graph(images)
        
        if len(G.edges) == 0:
            print("无法找到图像匹配，使用简单拼接")
            return self.simple_concatenate(images)
        
        # 处理不连通的图
        if not nx.is_connected(G):
            components = list(nx.connected_components(G))
            largest_component = max(components, key=len)
            G = G.subgraph(largest_component).copy()
            images = [images[i] for i in sorted(largest_component)]
            print(f"使用最大连通分量: {len(images)} 张图像")
        
        # 选择参考图像
        degrees = dict(G.degree())
        reference = max(degrees, key=degrees.get)
        print(f"参考图像: {reference}")
        
        # 束调整
        homographies = self.bundle_adjustment_conservative(images, G, match_results, reference)
        
        # 计算画布尺寸
        canvas_width, canvas_height, x_min, y_min = self.compute_canvas_bounds(images, homographies)
        
        # 限制画布大小
        max_canvas = 6000
        if canvas_width > max_canvas or canvas_height > max_canvas:
            scale = min(max_canvas / canvas_width, max_canvas / canvas_height)
            canvas_width = int(canvas_width * scale)
            canvas_height = int(canvas_height * scale)
            
            scale_matrix = np.diag([scale, scale, 1])
            for key in homographies:
                homographies[key] = scale_matrix @ homographies[key]
            x_min = int(x_min * scale)
            y_min = int(y_min * scale)
        
        # 创建偏移矩阵
        H_offset = np.array([[1, 0, -x_min], [0, 1, -y_min], [0, 0, 1]], dtype=np.float32)
        
        print(f"画布尺寸: {canvas_width} x {canvas_height}")
        
        # 按最优顺序拼接图像
        result = None
        result_mask = None
        
        # 从参考图像开始，按连接强度顺序处理
        processed = set()
        queue = [(reference, 0)]
        
        while queue:
            current_id, dist = queue.pop(0)
            if current_id in processed:
                continue
                
            processed.add(current_id)
            
            if current_id not in homographies:
                continue
                
            img = images[current_id]
            H_total = H_offset @ homographies[current_id]
            
            # 变换图像
            warped = cv2.warpPerspective(img, H_total, (canvas_width, canvas_height))
            h, w = img.shape[:2]
            mask = cv2.warpPerspective(np.ones((h, w), dtype=np.uint8) * 255, 
                                     H_total, (canvas_width, canvas_height))
            
            if result is None:
                # 第一张图像
                result = warped.copy()
                result_mask = mask.copy()
            else:
                # 使用改进的融合
                result = self.improved_blending(result, result_mask, warped, mask)
                result_mask = cv2.bitwise_or(result_mask, mask)
            
            # 添加邻居到队列
            for neighbor in G.neighbors(current_id):
                if neighbor not in processed:
                    queue.append((neighbor, dist + 1))
            
            print(f"已拼接图像 {current_id}")
        
        print("多图像拼接完成")
        return result
    
    def simple_concatenate(self, images):
        """
        简单拼接备用方案
        """
        if len(images) == 1:
            return images[0]
        
        # 尝试智能排列而不是简单水平拼接
        if len(images) == 2:
            # 两张图像尝试垂直或水平拼接
            h1, w1 = images[0].shape[:2]
            h2, w2 = images[1].shape[:2]
            
            # 选择更合适的拼接方向
            if abs(h1 - h2) < abs(w1 - w2):
                # 垂直拼接
                target_width = max(w1, w2)
                img1_resized = cv2.resize(images[0], (target_width, int(h1 * target_width / w1)))
                img2_resized = cv2.resize(images[1], (target_width, int(h2 * target_width / w2)))
                result = np.zeros((img1_resized.shape[0] + img2_resized.shape[0], target_width, 3), dtype=np.uint8)
                result[:img1_resized.shape[0], :] = img1_resized
                result[img1_resized.shape[0]:, :] = img2_resized
                return result
        
        # 默认水平拼接
        heights = [img.shape[0] for img in images]
        target_height = int(np.median(heights))
        
        resized_images = []
        total_width = 0
        
        for img in images:
            h, w = img.shape[:2]
            if h != target_height:
                new_w = int(w * target_height / h)
                img = cv2.resize(img, (new_w, target_height))
            resized_images.append(img)
            total_width += img.shape[1]
        
        result = np.zeros((target_height, total_width, 3), dtype=np.uint8)
        current_x = 0
        
        for img in resized_images:
            w = img.shape[1]
            result[:, current_x:current_x+w] = img
            current_x += w
        
        return result

def stitch_case(case_name, input_dir, output_dir):
    """处理单个测试案例"""
    image_paths = sorted(glob.glob(os.path.join(input_dir, case_name, "*")))
    images = [cv2.imread(p) for p in image_paths]
    
    if any(img is None for img in images) or len(images) < 2:
        print(f"[{case_name}] 跳过: 图像数量不足或无法读取")
        return

    print(f"[{case_name}] 开始拼接 {len(images)} 张图像...")
    
    stitcher = MultiImageStitcher()
    stitched_image = stitcher.create_mosaic(images)
    
    if stitched_image is None:
        print(f"[{case_name}] 拼接失败")
        return

    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, f"{case_name}.JPG")
    cv2.imwrite(output_path, stitched_image)
    print(f"[{case_name}] 完成: 保存到 {output_path}")

def main():
    """主函数"""
    input_root = "data/task2_multiview"
    output_root = "output/task2_multiview"
    
    os.makedirs(output_root, exist_ok=True)
    
    # 获取所有测试案例
    cases = [name for name in os.listdir(input_root) 
             if os.path.isdir(os.path.join(input_root, name))]
    
    if not cases:
        print("在'data'目录中未找到测试案例")
        return

    # 处理每个案例
    for case in sorted(cases):
        stitch_case(case, input_root, output_root)

if __name__ == "__main__":
    main()
