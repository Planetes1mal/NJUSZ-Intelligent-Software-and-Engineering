import cv2
import numpy as np
import glob
import os

class TwoImageStitcher:
    def __init__(self):
        # 初始化SIFT特征检测器
        self.sift = cv2.SIFT_create(nfeatures=1000)
        # 初始化ORB作为备选
        self.orb = cv2.ORB_create(nfeatures=1000)
        
    def get_correspondences(self, img1, img2):
        """
        (1) 获取对应点 - 使用OpenCV的特征检测和匹配
        """
        # 转换为灰度图
        gray1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
        gray2 = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)
        
        # 首先尝试SIFT
        kp1, des1 = self.sift.detectAndCompute(gray1, None)
        kp2, des2 = self.sift.detectAndCompute(gray2, None)
        
        if des1 is None or des2 is None or len(des1) < 4 or len(des2) < 4:
            print("SIFT特征点不足，尝试ORB...")
            # 如果SIFT失败，尝试ORB
            kp1, des1 = self.orb.detectAndCompute(gray1, None)
            kp2, des2 = self.orb.detectAndCompute(gray2, None)
            use_sift = False
        else:
            use_sift = True
            
        if des1 is None or des2 is None or len(des1) < 4 or len(des2) < 4:
            print("无法检测到足够的特征点")
            return None, None
            
        print(f"检测到特征点: img1={len(des1)}, img2={len(des2)}")
        
        # 特征匹配
        if use_sift:
            # 对于SIFT使用FLANN匹配器
            FLANN_INDEX_KDTREE = 1
            index_params = dict(algorithm=FLANN_INDEX_KDTREE, trees=5)
            search_params = dict(checks=50)
            matcher = cv2.FlannBasedMatcher(index_params, search_params)
        else:
            # 对于ORB使用BF匹配器
            matcher = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=False)
        
        matches = matcher.knnMatch(des1, des2, k=2)
        
        # 应用Lowe's ratio test
        good_matches = []
        for match_pair in matches:
            if len(match_pair) == 2:
                m, n = match_pair
                if m.distance < 0.7 * n.distance:
                    good_matches.append(m)
        
        print(f"找到 {len(good_matches)} 个好的匹配")
        
        if len(good_matches) < 4:
            print("匹配点数量不足")
            return None, None
        
        # 提取匹配点坐标
        src_pts = np.float32([kp1[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
        dst_pts = np.float32([kp2[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)
        
        return src_pts, dst_pts
    
    def compute_homography(self, src_pts, dst_pts):
        """
        (2) 计算单应性矩阵 - 使用OpenCV的findHomography函数
        """
        if src_pts is None or dst_pts is None:
            return None
            
        # 使用RANSAC计算单应性矩阵
        H, mask = cv2.findHomography(
            src_pts, dst_pts, 
            cv2.RANSAC, 
            ransacReprojThreshold=5.0
        )
        
        if H is None:
            print("无法计算单应性矩阵")
            return None
            
        # 计算内点数量
        inliers = np.sum(mask) if mask is not None else 0
        inlier_ratio = inliers / len(src_pts)
        print(f"单应性矩阵计算成功: {inliers}/{len(src_pts)} 内点 ({inlier_ratio:.2%})")
        
        return H
    
    def warp_image(self, img, H, reference_shape):
        """
        (3) 图像变换 - 使用OpenCV的warpPerspective函数
        """
        h, w = img.shape[:2]
        ref_h, ref_w = reference_shape[:2]
        
        # 计算变换后的角点来确定输出图像大小
        corners = np.float32([[0, 0], [w, 0], [w, h], [0, h]]).reshape(-1, 1, 2)
        transformed_corners = cv2.perspectiveTransform(corners, H)
        
        # 计算包含两个图像的边界框
        all_corners = np.concatenate([
            transformed_corners,
            np.float32([[0, 0], [ref_w, 0], [ref_w, ref_h], [0, ref_h]]).reshape(-1, 1, 2)
        ], axis=0)
        
        x_coords = all_corners[:, 0, 0]
        y_coords = all_corners[:, 0, 1]
        
        x_min, x_max = int(np.floor(x_coords.min())), int(np.ceil(x_coords.max()))
        y_min, y_max = int(np.floor(y_coords.min())), int(np.ceil(y_coords.max()))
        
        # 计算偏移量和输出图像尺寸
        offset_x, offset_y = -x_min, -y_min
        output_width = x_max - x_min
        output_height = y_max - y_min
        
        # 创建变换矩阵（包含偏移）
        translation = np.array([
            [1, 0, offset_x],
            [0, 1, offset_y],
            [0, 0, 1]
        ], dtype=np.float32)
        
        H_with_offset = translation @ H
        
        # 应用透视变换
        warped_img = cv2.warpPerspective(img, H_with_offset, (output_width, output_height))
        
        return warped_img, (offset_x, offset_y), (output_width, output_height)
    
    def create_mosaic(self, img1, img2, warped_img1, offset, output_size):
        """
        (4) 创建输出马赛克 - 简单的overlay合并
        """
        offset_x, offset_y = offset
        output_width, output_height = output_size
        
        # 创建输出图像
        result = np.zeros((output_height, output_width, 3), dtype=np.uint8)
        
        # 首先放置变换后的img1
        result = warped_img1.copy()
        
        # 计算img2在输出图像中的位置
        img2_start_x = offset_x
        img2_start_y = offset_y
        img2_end_x = img2_start_x + img2.shape[1]
        img2_end_y = img2_start_y + img2.shape[0]
        
        # 确保边界在有效范围内
        img2_start_x = max(0, img2_start_x)
        img2_start_y = max(0, img2_start_y)
        img2_end_x = min(output_width, img2_end_x)
        img2_end_y = min(output_height, img2_end_y)
        
        # 计算img2中对应的区域
        src_start_x = max(0, -offset_x)
        src_start_y = max(0, -offset_y)
        src_end_x = src_start_x + (img2_end_x - img2_start_x)
        src_end_y = src_start_y + (img2_end_y - img2_start_y)
        
        # 将img2覆盖到结果图像上
        if img2_start_x < img2_end_x and img2_start_y < img2_end_y:
            # 获取重叠区域
            img2_region = img2[src_start_y:src_end_y, src_start_x:src_end_x]
            result_region = result[img2_start_y:img2_end_y, img2_start_x:img2_end_x]
            
            # 创建掩码：img2中非黑色的像素
            mask = np.any(img2_region != 0, axis=2)
            
            # 只在img2有内容的地方进行覆盖
            result[img2_start_y:img2_end_y, img2_start_x:img2_end_x][mask] = img2_region[mask]
        
        return result
    
    def stitch_two_images(self, img1, img2):
        """
        主要的两图像拼接函数
        """
        print("开始两图像拼接...")
        
        # 步骤1: 获取对应点
        src_pts, dst_pts = self.get_correspondences(img1, img2)
        if src_pts is None:
            print("无法找到足够的对应点，拼接失败")
            return None
        
        # 步骤2: 计算单应性矩阵
        H = self.compute_homography(src_pts, dst_pts)
        if H is None:
            print("无法计算单应性矩阵，拼接失败")
            return None
        
        # 步骤3: 图像变换
        warped_img1, offset, output_size = self.warp_image(img1, H, img2.shape)
        
        # 步骤4: 创建马赛克
        result = self.create_mosaic(img1, img2, warped_img1, offset, output_size)
        
        print("拼接完成")
        return result

def stitch_case(case_name, input_dir, output_dir):
    """处理单个测试案例"""
    image_paths = sorted(glob.glob(os.path.join(input_dir, case_name, "*")))
    images = [cv2.imread(p) for p in image_paths]
    
    if any(img is None for img in images) or len(images) != 2:
        print(f"[{case_name}] 跳过: 图像数量不足或无法读取")
        return

    print(f"[{case_name}] 开始处理...")
    
    # 创建拼接器并处理
    stitcher = TwoImageStitcher()
    stitched_image = stitcher.stitch_two_images(images[0], images[1])
    
    if stitched_image is None:
        print(f"[{case_name}] 拼接失败")
        return

    # 保存结果
    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, f"{case_name}.JPG")
    cv2.imwrite(output_path, stitched_image)
    print(f"[{case_name}] 完成: 保存到 {output_path}")

def main():
    """主函数"""
    input_root = "data/task1_pairwise"
    output_root = "output/task1_pairwise"
    
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
