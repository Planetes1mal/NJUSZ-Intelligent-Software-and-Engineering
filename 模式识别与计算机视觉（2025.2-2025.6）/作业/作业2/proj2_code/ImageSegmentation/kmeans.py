import numpy as np
import cv2
import matplotlib.pyplot as plt
import os
import random

def euclidean_distance(point1, point2):
    """
    计算两点之间的欧几里得距离
    """
    return np.sqrt(np.sum((point1 - point2) ** 2))

def initialize_centroids(data, k):
    """
    随机选择k个中心点
    """
    # 获取数据集大小
    n_samples = data.shape[0]
    # 随机选择k个不同的索引
    centroids_indices = np.random.choice(n_samples, k, replace=False)
    # 返回对应的数据点作为初始中心点
    centroids = data[centroids_indices]
    return centroids

def assign_clusters(data, centroids):
    """
    将每个数据点分配到最近的中心点所在的簇 (向量化实现)
    """
    # 计算每个数据点到每个中心点的距离
    # 使用广播机制计算欧几里得距离
    distances = np.sqrt(((data[:, np.newaxis, :] - centroids[np.newaxis, :, :])**2).sum(axis=2))
    # 找到最近的中心点的索引
    clusters = np.argmin(distances, axis=1)
    return clusters

def update_centroids(data, clusters, k):
    """
    根据分配后的簇更新中心点 (向量化实现)
    """
    # 初始化新的中心点
    new_centroids = np.zeros((k, data.shape[1]), dtype=data.dtype)
    # 对每个簇
    for i in range(k):
        # 获取簇中的点
        mask = clusters == i
        if np.any(mask):
            # 计算均值作为新的中心点
            new_centroids[i] = np.mean(data[mask], axis=0)
    return new_centroids

def has_converged(old_centroids, new_centroids, epsilon):
    """
    检查算法是否收敛 (向量化实现)
    """
    # 计算所有中心点的移动距离总和
    distances = np.sqrt(np.sum((old_centroids - new_centroids)**2, axis=1))
    return np.sum(distances) < epsilon

def kmeans(data, k, max_iters=100, epsilon=1.0):
    """
    K-means聚类算法 (优化版)
    
    参数:
    data: 输入数据，每行是一个样本
    k: 聚类数量
    max_iters: 最大迭代次数
    epsilon: 收敛阈值
    
    返回:
    clusters: 每个数据点所属的簇
    centroids: 最终的中心点
    """
    # 初始化中心点
    centroids = initialize_centroids(data, k)
    
    # 对于大型数据集，可以考虑随机抽样来加速
    sample_size = min(100000, len(data))  # 限制样本数量
    
    # 迭代优化
    for i in range(max_iters):
        # 如果数据集很大，随机抽样用于更新中心点
        if len(data) > sample_size:
            idx = np.random.choice(len(data), sample_size, replace=False)
            sample_data = data[idx]
            # 分配簇 (只对样本)
            sample_clusters = assign_clusters(sample_data, centroids)
            # 更新中心点 (基于样本)
            new_centroids = update_centroids(sample_data, sample_clusters, k)
        else:
            # 分配簇
            clusters = assign_clusters(data, centroids)
            # 更新中心点
            new_centroids = update_centroids(data, clusters, k)
        
        # 检查是否收敛
        if has_converged(centroids, new_centroids, epsilon):
            centroids = new_centroids
            break
            
        centroids = new_centroids
    
    # 最终将所有数据点分配到簇
    clusters = assign_clusters(data, centroids)
    
    return clusters, centroids

def segment_image(image_path, k_values, max_iters=100, epsilon=1.0):
    """
    使用K-means对图像进行分割，并显示不同K值的结果
    
    参数:
    image_path: 图像路径
    k_values: K值列表
    max_iters: 最大迭代次数
    epsilon: 收敛阈值
    
    返回:
    results: 分割后的图像列表
    """
    # 读取图像
    img = cv2.imread(image_path)
    
    """
    # 缩小图像以加速处理（可选）
    max_dim = 400  # 限制最大尺寸
    h, w = img.shape[:2]
    if max(h, w) > max_dim:
        # 计算缩放比例
        scale = max_dim / max(h, w)
        img = cv2.resize(img, (int(w * scale), int(h * scale)))
    """

    # 图像二维像素转换为一维
    data = img.reshape((-1, 3))
    data = np.float32(data)
    
    # 存储结果
    results = [cv2.cvtColor(img, cv2.COLOR_BGR2RGB)]  # 原始图像
    
    # 对每个K值进行聚类
    for k in k_values:
        print(f"正在处理K={k}...")
        
        # 使用自定义K-means
        clusters, centroids = kmeans(data, k, max_iters, epsilon)
        
        # 像素替换为所属簇的中心点颜色
        centroids = np.uint8(centroids)
        segmented_data = centroids[clusters]
        segmented_image = segmented_data.reshape(img.shape)
        
        # 转换为RGB
        segmented_image = cv2.cvtColor(segmented_image, cv2.COLOR_BGR2RGB)
        results.append(segmented_image)
    
    return results

def save_segmentation_results(image_path, output_dir, k_values, max_iters=100, epsilon=1.0):
    """
    对图像进行分割并保存结果
    
    参数:
    image_path: 图像路径
    output_dir: 输出目录
    k_values: K值列表
    max_iters: 最大迭代次数
    epsilon: 收敛阈值
    """
    # 确保输出目录存在
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    # 获取图像文件名（不含扩展名）
    image_name = os.path.splitext(os.path.basename(image_path))[0]
    
    # 分割图像
    results = segment_image(image_path, k_values, max_iters, epsilon)
    
    # 创建标题
    titles = ['Original'] + [f'K={k}' for k in k_values]
    
    # 创建子图布局
    nrows = 2
    ncols = 3
    fig, axes = plt.subplots(nrows, ncols, figsize=(15, 10))
    
    # 在子图上显示图像
    for i, (ax, img, title) in enumerate(zip(axes.flatten(), results, titles)):
        if i < len(results):
            ax.imshow(img)
            ax.set_title(title)
            ax.axis('off')
    
    # 保存图像
    output_path = os.path.join(output_dir, f'{image_name}_kmeans.png')
    plt.tight_layout()
    plt.savefig(output_path, dpi=300)
    plt.close()
    
    print(f"结果已保存到 {output_path}") 