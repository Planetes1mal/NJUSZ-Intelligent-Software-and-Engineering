import numpy as np
import cv2
import matplotlib.pyplot as plt
import os
import random
from collections import deque
import time

def find_seed_points(image, n_seeds=10, strategy='random'):
    """
    寻找种子点
    
    参数:
    image: 输入图像
    n_seeds: 种子点数量
    strategy: 种子点选择策略，可以是'random'(随机)或'grid'(网格)
    
    返回:
    seeds: 种子点列表 [(x1,y1), (x2,y2), ...]
    """
    height, width = image.shape[:2]
    seeds = []
    
    if strategy == 'random':
        # 随机选择种子点
        for _ in range(n_seeds):
            x = random.randint(0, width-1)
            y = random.randint(0, height-1)
            seeds.append((x, y))
    
    elif strategy == 'grid':
        # 在网格上均匀选择种子点
        rows = int(np.sqrt(n_seeds))
        cols = int(np.sqrt(n_seeds))
        
        row_step = height // (rows + 1)
        col_step = width // (cols + 1)
        
        for i in range(1, rows + 1):
            for j in range(1, cols + 1):
                y = i * row_step
                x = j * col_step
                seeds.append((x, y))
    
    return seeds

def region_growing_abs_diff(image, seed_points, threshold, connectivity=8):
    """
    基于绝对差值的区域生长算法
    
    参数:
    image: 输入图像
    seed_points: 种子点列表
    threshold: 灰度绝对差值阈值
    connectivity: 连通性，可以是4或8
    
    返回:
    segmented: 分割结果，每个区域用不同的标签
    """
    if len(image.shape) == 3:
        # 转换为灰度图
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    else:
        gray = image.copy()
    
    height, width = gray.shape
    segmented = np.zeros((height, width), dtype=np.int32)
    
    # 定义8邻域偏移
    if connectivity == 8:
        neighbors = [(-1,-1), (-1,0), (-1,1), (0,-1), (0,1), (1,-1), (1,0), (1,1)]
    else:  # 4邻域
        neighbors = [(-1,0), (0,-1), (0,1), (1,0)]
    
    # 为每个种子点分配一个唯一区域ID
    region_id = 1
    gray = gray.astype(np.int32)  # 避免类型转换带来的开销
    
    for seed_x, seed_y in seed_points:
        # 如果种子点已经被分配，则跳过
        if segmented[seed_y, seed_x] != 0:
            continue
        
        # 当前种子点的灰度值
        seed_value = gray[seed_y, seed_x]
        
        # 创建一个队列用于广度优先搜索
        queue = deque([(seed_x, seed_y)])
        segmented[seed_y, seed_x] = region_id
        
        while queue:
            x, y = queue.popleft()
            
            # 检查邻居
            for dx, dy in neighbors:
                nx, ny = x + dx, y + dy
                
                # 检查边界
                if 0 <= nx < width and 0 <= ny < height:
                    # 检查是否已分配和生长准则
                    if segmented[ny, nx] == 0 and abs(gray[ny, nx] - seed_value) <= threshold:
                        segmented[ny, nx] = region_id
                        queue.append((nx, ny))
        
        region_id += 1
    
    return segmented

def region_growing_mean_diff(image, seed_points, threshold, connectivity=8):
    """
    基于区域均值差值的区域生长算法
    
    参数:
    image: 输入图像
    seed_points: 种子点列表
    threshold: 与区域均值的差值阈值
    connectivity: 连通性，可以是4或8
    
    返回:
    segmented: 分割结果，每个区域用不同的标签
    """
    if len(image.shape) == 3:
        # 转换为灰度图
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    else:
        gray = image.copy()
    
    height, width = gray.shape
    segmented = np.zeros((height, width), dtype=np.int32)
    
    # 定义8邻域偏移
    if connectivity == 8:
        neighbors = [(-1,-1), (-1,0), (-1,1), (0,-1), (0,1), (1,-1), (1,0), (1,1)]
    else:  # 4邻域
        neighbors = [(-1,0), (0,-1), (0,1), (1,0)]
    
    # 为每个种子点分配一个唯一区域ID
    region_id = 1
    gray = gray.astype(np.float32)  # 避免重复类型转换
    
    for seed_x, seed_y in seed_points:
        # 如果种子点已经被分配，则跳过
        if segmented[seed_y, seed_x] != 0:
            continue
        
        # 当前区域的像素总和和计数，用于高效计算均值
        region_sum = float(gray[seed_y, seed_x])
        region_count = 1
        region_mean = region_sum / region_count
        
        # 创建一个队列用于广度优先搜索
        queue = deque([(seed_x, seed_y)])
        segmented[seed_y, seed_x] = region_id
        
        while queue:
            x, y = queue.popleft()
            
            # 检查邻居
            for dx, dy in neighbors:
                nx, ny = x + dx, y + dy
                
                # 检查边界
                if 0 <= nx < width and 0 <= ny < height:
                    # 检查是否已分配和生长准则
                    pixel_value = gray[ny, nx]
                    if segmented[ny, nx] == 0 and abs(pixel_value - region_mean) <= threshold:
                        segmented[ny, nx] = region_id
                        queue.append((nx, ny))
                        
                        # 更新区域均值 (更高效的方式)
                        region_sum += pixel_value
                        region_count += 1
                        region_mean = region_sum / region_count
        
        region_id += 1
    
    return segmented

def region_growing_color_diff(image, seed_points, threshold, connectivity=8):
    """
    基于颜色空间的区域生长算法
    
    参数:
    image: 输入彩色图像 (BGR)
    seed_points: 种子点列表
    threshold: 颜色差异阈值
    connectivity: 连通性，可以是4或8
    
    返回:
    segmented: 分割结果，每个区域用不同的标签
    """
    if len(image.shape) < 3:
        # 如果是灰度图，转换为三通道
        image = cv2.cvtColor(image, cv2.COLOR_GRAY2BGR)
    
    # 转换到HSV空间，颜色更具有感知意义
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV).astype(np.float32)
    
    height, width = image.shape[:2]
    segmented = np.zeros((height, width), dtype=np.int32)
    
    # 定义8邻域偏移
    if connectivity == 8:
        neighbors = [(-1,-1), (-1,0), (-1,1), (0,-1), (0,1), (1,-1), (1,0), (1,1)]
    else:  # 4邻域
        neighbors = [(-1,0), (0,-1), (0,1), (1,0)]
    
    # 为每个种子点分配一个唯一区域ID
    region_id = 1
    
    for seed_x, seed_y in seed_points:
        # 如果种子点已经被分配，则跳过
        if segmented[seed_y, seed_x] != 0:
            continue
        
        # 当前种子点的HSV值
        seed_value = hsv[seed_y, seed_x]
        
        # 创建一个队列用于广度优先搜索
        queue = deque([(seed_x, seed_y)])
        segmented[seed_y, seed_x] = region_id
        
        while queue:
            x, y = queue.popleft()
            
            # 检查邻居
            for dx, dy in neighbors:
                nx, ny = x + dx, y + dy
                
                # 检查边界
                if 0 <= nx < width and 0 <= ny < height:
                    # 检查是否已分配和生长准则
                    if segmented[ny, nx] == 0:
                        pixel_value = hsv[ny, nx]
                        
                        # 计算颜色差异 (注意H通道是循环的)
                        h_diff = min(abs(pixel_value[0] - seed_value[0]), 
                                     180 - abs(pixel_value[0] - seed_value[0]))
                        s_diff = abs(pixel_value[1] - seed_value[1])
                        v_diff = abs(pixel_value[2] - seed_value[2])
                        
                        # 加权合并差异
                        color_diff = h_diff * 0.5 + s_diff * 0.3 + v_diff * 0.2
                        
                        if color_diff <= threshold:
                            segmented[ny, nx] = region_id
                            queue.append((nx, ny))
        
        region_id += 1
    
    return segmented

def compute_lbp(gray):
    """
    计算整幅图像的LBP特征 (优化版)
    
    参数:
    gray: 灰度图像
    
    返回:
    lbp_image: LBP特征图像
    """
    height, width = gray.shape
    # 预分配数组
    lbp = np.zeros((height, width), dtype=np.uint8)
    
    # 计算内部区域的LBP (忽略边界1像素)
    for y in range(1, height-1):
        for x in range(1, width-1):
            center = gray[y, x]
            # 使用位操作计算LBP
            code = 0
            code |= (gray[y-1, x-1] >= center) << 7
            code |= (gray[y-1, x] >= center) << 6
            code |= (gray[y-1, x+1] >= center) << 5
            code |= (gray[y, x+1] >= center) << 4
            code |= (gray[y+1, x+1] >= center) << 3
            code |= (gray[y+1, x] >= center) << 2
            code |= (gray[y+1, x-1] >= center) << 1
            code |= (gray[y, x-1] >= center) << 0
            lbp[y, x] = code
    
    return lbp

def region_growing_texture(image, seed_points, threshold, connectivity=8, window_size=3):
    """
    基于纹理特征的区域生长算法
    
    参数:
    image: 输入图像
    seed_points: 种子点列表
    threshold: 纹理差异阈值
    connectivity: 连通性，可以是4或8
    window_size: 计算纹理特征的窗口大小
    
    返回:
    segmented: 分割结果，每个区域用不同的标签
    """
    if len(image.shape) == 3:
        # 转换为灰度图
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    else:
        gray = image.copy()
    
    # 计算整幅图像的LBP特征 (只计算一次)
    texture = compute_lbp(gray)
    
    # 将纹理特征的直方图缓存起来，避免重复计算
    hist_cache = {}
    
    height, width = gray.shape
    segmented = np.zeros((height, width), dtype=np.int32)
    
    # 定义8邻域偏移
    if connectivity == 8:
        neighbors = [(-1,-1), (-1,0), (-1,1), (0,-1), (0,1), (1,-1), (1,0), (1,1)]
    else:  # 4邻域
        neighbors = [(-1,0), (0,-1), (0,1), (1,0)]
    
    # 使用更小的窗口大小来加速计算
    window_size = min(window_size, 3)
    half_window = window_size // 2
    
    # 为每个种子点分配一个唯一区域ID
    region_id = 1
    
    for seed_x, seed_y in seed_points:
        # 确保种子点不在边界上
        if seed_x <= half_window or seed_x >= width-half_window or \
           seed_y <= half_window or seed_y >= height-half_window:
           continue
            
        # 如果种子点已经被分配，则跳过
        if segmented[seed_y, seed_x] != 0:
            continue
        
        # 计算种子点周围窗口的LBP直方图
        seed_key = (seed_y, seed_x)
        if seed_key not in hist_cache:
            seed_window = texture[seed_y-half_window:seed_y+half_window+1, 
                                seed_x-half_window:seed_x+half_window+1]
            seed_hist = cv2.calcHist([seed_window], [0], None, [256], [0, 256])
            seed_hist = cv2.normalize(seed_hist, seed_hist).flatten()
            hist_cache[seed_key] = seed_hist
        else:
            seed_hist = hist_cache[seed_key]
        
        # 创建一个队列用于广度优先搜索
        queue = deque([(seed_x, seed_y)])
        segmented[seed_y, seed_x] = region_id
        
        # 限制队列大小以防止过度生长
        max_region_size = 10000  # 限制区域大小
        region_size = 1
        
        while queue and region_size < max_region_size:
            x, y = queue.popleft()
            
            # 检查邻居
            for dx, dy in neighbors:
                nx, ny = x + dx, y + dy
                
                # 检查边界（确保有足够空间计算窗口）
                if half_window < nx < width-half_window and half_window < ny < height-half_window:
                    # 检查是否已分配
                    if segmented[ny, nx] == 0:
                        # 计算当前点周围窗口的LBP直方图
                        pixel_key = (ny, nx)
                        if pixel_key not in hist_cache:
                            window = texture[ny-half_window:ny+half_window+1, 
                                            nx-half_window:nx+half_window+1]
                            hist = cv2.calcHist([window], [0], None, [256], [0, 256])
                            hist = cv2.normalize(hist, hist).flatten()
                            hist_cache[pixel_key] = hist
                        else:
                            hist = hist_cache[pixel_key]
                        
                        # 计算直方图相似度 (巴氏距离)
                        bc = cv2.compareHist(seed_hist, hist, cv2.HISTCMP_BHATTACHARYYA)
                        
                        # 巴氏距离越小，相似度越高
                        if bc < threshold:
                            segmented[ny, nx] = region_id
                            queue.append((nx, ny))
                            region_size += 1
        
        region_id += 1
        
        # 清除缓存，防止内存溢出
        if len(hist_cache) > 1000:
            hist_cache.clear()
    
    return segmented

def resize_image_if_needed(image, max_dim=400):
    """
    如果图像尺寸过大，调整图像大小
    
    参数:
    image: 输入图像
    max_dim: 最大尺寸
    
    返回:
    resized_image: 调整大小后的图像
    scale: 缩放比例
    """
    height, width = image.shape[:2]
    scale = 1.0
    
    if max(height, width) > max_dim:
        scale = max_dim / max(height, width)
        new_width = int(width * scale)
        new_height = int(height * scale)
        resized_image = cv2.resize(image, (new_width, new_height))
        return resized_image, scale
    
    return image, scale

def visualize_segmentation(image, segmented, title='Region Growing Segmentation'):
    """
    可视化分割结果
    
    参数:
    image: 原始图像
    segmented: 分割结果
    title: 图像标题
    """
    # 为每个区域随机分配颜色
    n_regions = np.max(segmented)
    colors = np.random.randint(0, 255, (n_regions + 1, 3), dtype=np.uint8)
    colors[0] = [0, 0, 0]  # 背景为黑色
    
    # 创建彩色分割图像
    colored_segmented = np.zeros((segmented.shape[0], segmented.shape[1], 3), dtype=np.uint8)
    for i in range(1, n_regions + 1):
        colored_segmented[segmented == i] = colors[i]
    
    # 显示原始图像和分割结果
    plt.figure(figsize=(12, 6))
    
    plt.subplot(121)
    plt.imshow(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
    plt.title('Original Image')
    plt.axis('off')
    
    plt.subplot(122)
    plt.imshow(colored_segmented)
    plt.title(title)
    plt.axis('off')
    
    return colored_segmented

def save_region_growing_results(image_path, output_dir, n_seeds=30, thresholds=None, criteria=None):
    """
    应用不同生长准则和阈值，保存区域生长结果
    
    参数:
    image_path: 图像路径
    output_dir: 输出目录
    n_seeds: 种子点数量
    thresholds: 阈值列表，每个准则对应一个阈值
    criteria: 生长准则列表
    """
    start_time = time.time()
    
    # 确保输出目录存在
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    # 获取图像文件名（不含扩展名）
    image_name = os.path.splitext(os.path.basename(image_path))[0]
    
    # 读取图像
    img = cv2.imread(image_path)
    
    # 调整图像大小以加速处理
    img, scale = resize_image_if_needed(img, max_dim=400)
    
    # 默认准则和阈值
    if criteria is None:
        criteria = ['abs_diff', 'mean_diff', 'color_diff', 'texture']
    
    if thresholds is None:
        thresholds = {
            'abs_diff': 15,
            'mean_diff': 20,
            'color_diff': 40,
            'texture': 0.35
        }
    
    # 选择种子点
    seed_points = find_seed_points(img, n_seeds=n_seeds, strategy='grid')
    
    # 为每个准则创建一张图
    for criterion in criteria:
        criterion_start_time = time.time()
        threshold = thresholds[criterion]
        
        if criterion == 'abs_diff':
            title = f'Absolute Difference (threshold={threshold})'
            segmented = region_growing_abs_diff(img, seed_points, threshold)
        elif criterion == 'mean_diff':
            title = f'Mean Difference (threshold={threshold})'
            segmented = region_growing_mean_diff(img, seed_points, threshold)
        elif criterion == 'color_diff':
            title = f'Color Difference (threshold={threshold})'
            segmented = region_growing_color_diff(img, seed_points, threshold)
        elif criterion == 'texture':
            title = f'Texture Difference (threshold={threshold})'
            segmented = region_growing_texture(img, seed_points, threshold)
        
        criterion_time = time.time() - criterion_start_time
        print(f"{criterion} 处理耗时: {criterion_time:.2f}秒")
        
        # 可视化并保存结果
        colored_segmented = visualize_segmentation(img, segmented, title)
        
        # 保存图像
        output_path = os.path.join(output_dir, f'{image_name}_{criterion}.png')
        plt.savefig(output_path, dpi=300, bbox_inches='tight')
        plt.close()
        
        print(f"保存到 {output_path}")
    
    # 创建对比图
    plt.figure(figsize=(15, 10))
    plt.subplot(231)
    plt.imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    plt.title('Original')
    plt.axis('off')
    
    all_segmented = []
    
    for i, criterion in enumerate(criteria):
        criterion_start_time = time.time()
        threshold = thresholds[criterion]
        
        if criterion == 'abs_diff':
            title = f'Absolute Diff (t={threshold})'
            segmented = region_growing_abs_diff(img, seed_points, threshold)
        elif criterion == 'mean_diff':
            title = f'Mean Diff (t={threshold})'
            segmented = region_growing_mean_diff(img, seed_points, threshold)
        elif criterion == 'color_diff':
            title = f'Color Diff (t={threshold})'
            segmented = region_growing_color_diff(img, seed_points, threshold)
        elif criterion == 'texture':
            title = f'Texture Diff (t={threshold})'
            segmented = region_growing_texture(img, seed_points, threshold)
        
        all_segmented.append(segmented)
        
        criterion_time = time.time() - criterion_start_time
        print(f"对比图 {criterion} 处理耗时: {criterion_time:.2f}秒")
        
        # 为每个区域随机分配颜色
        n_regions = np.max(segmented)
        colors = np.random.randint(0, 255, (n_regions + 1, 3), dtype=np.uint8)
        colors[0] = [0, 0, 0]  # 背景为黑色
        
        # 创建彩色分割图像
        colored_segmented = np.zeros((segmented.shape[0], segmented.shape[1], 3), dtype=np.uint8)
        for j in range(1, n_regions + 1):
            colored_segmented[segmented == j] = colors[j]
        
        plt.subplot(2, 3, i+2)
        plt.imshow(colored_segmented)
        plt.title(title)
        plt.axis('off')
    
    # 保存对比图
    output_path = os.path.join(output_dir, f'{image_name}_comparison.png')
    plt.tight_layout()
    plt.savefig(output_path, dpi=300)
    plt.close()
    
    total_time = time.time() - start_time
    print(f"总处理耗时: {total_time:.2f}秒")
    print(f"保存对比图到 {output_path}") 