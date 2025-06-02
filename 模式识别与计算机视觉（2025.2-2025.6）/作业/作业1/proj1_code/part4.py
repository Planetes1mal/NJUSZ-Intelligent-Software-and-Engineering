#!/usr/bin/python3

from typing import Tuple

import numpy as np

import numpy.fft as fft

from proj1_code.utils import load_image, save_image, PIL_resize, numpy_arr_to_PIL_image, PIL_image_to_numpy_arr, im2single, single2im

# 导入可视化所需库
import matplotlib.pyplot as plt
from matplotlib import gridspec

def compute_psnr(original: np.ndarray, compressed: np.ndarray) -> float:
    """
    计算原始图像和压缩图像之间的峰值信噪比(PSNR)
    
    Args:
        original: 原始图像数组
        compressed: 压缩后的图像数组
    
    Returns:
        psnr: 峰值信噪比，单位为分贝(dB)
    """
    try:
        # 确保两个图像具有相同的形状
        if original.shape != compressed.shape:
            raise ValueError(f"图像形状不匹配: {original.shape} vs {compressed.shape}")
        
        # 将图像转换为相同类型
        original = original.astype(np.float64)
        compressed = compressed.astype(np.float64)
        
        # 确保图像在相同范围内进行比较
        if original.max() > 1.0:
            original = original / 255.0
        if compressed.max() > 1.0:
            compressed = compressed / 255.0
        
        # 计算均方误差(MSE)
        mse = np.mean(np.square(original - compressed))
        
        # 避免除以零
        if mse < 1e-10:
            return float('inf')
        
        # 计算峰值信噪比
        max_pixel = 1.0
        psnr = 10 * np.log10((max_pixel ** 2) / mse)
        
        return psnr
    
    except Exception as e:
        print(f"计算PSNR时出错: {e}")
        return 0.0

def compress_frequency(image: np.ndarray, retention_ratio: float) -> np.ndarray:
    """
    使用频率压缩方法压缩图像
    
    Args:
        image: 输入图像数组，形状为(H, W, C)或(H, W)
        retention_ratio: 保留的低频成分比例，取值范围[0, 1]
    
    Returns:
        compressed_image: 压缩后的图像数组，形状与输入相同
    """
    # 检查图像是否为彩色图像
    is_color = len(image.shape) == 3
    
    # 如果是彩色图像，分别处理每个通道
    if is_color:
        h, w, c = image.shape
        compressed_image = np.zeros_like(image)
        
        for i in range(c):
            compressed_image[:, :, i] = compress_single_channel(image[:, :, i], retention_ratio)
    else:
        # 灰度图像直接处理
        compressed_image = compress_single_channel(image, retention_ratio)
    
    return compressed_image

def compress_single_channel(channel: np.ndarray, retention_ratio: float) -> np.ndarray:
    """
    压缩单通道图像
    
    Args:
        channel: 单通道图像数组
        retention_ratio: 保留的低频成分比例
    
    Returns:
        compressed_channel: 压缩后的单通道图像
    """
    try:
        # 确保数据类型为float，以便进行傅里叶变换
        channel = channel.astype(np.float64)
        
        # 执行2D傅里叶变换
        f_transform = fft.fft2(channel)
        
        # 将零频率分量移到中心
        f_shift = fft.fftshift(f_transform)
        
        # 获取图像尺寸
        h, w = channel.shape
        center_h, center_w = h // 2, w // 2
        
        # 计算保留的半径
        radius = max(1, int(min(center_h, center_w) * retention_ratio))
        
        # 创建掩码，仅保留中心区域(低频部分)
        mask = np.zeros((h, w), dtype=np.float64)
        y, x = np.ogrid[:h, :w]
        mask_area = ((y - center_h) ** 2 + (x - center_w) ** 2) <= (radius ** 2)
        mask[mask_area] = 1
        
        # 应用掩码，保留低频成分
        f_shift_masked = f_shift * mask
        
        # 将零频率分量移回原位
        f_transform_masked = fft.ifftshift(f_shift_masked)
        
        # 执行逆傅里叶变换
        compressed_channel = np.real(fft.ifft2(f_transform_masked))
        
        # 确保结果在原始图像的值范围内
        if channel.max() > 1.0:
            compressed_channel = np.clip(compressed_channel, 0, 255)
        else:
            compressed_channel = np.clip(compressed_channel, 0, 1)
            
        return compressed_channel
        
    except Exception as e:
        print(f"压缩过程中出错: {e}")
        # 出错时返回原始通道
        return channel

def visualize_spectrum(image: np.ndarray, log_scale: bool = True) -> np.ndarray:
    """
    可视化图像的频谱
    
    Args:
        image: 输入图像
        log_scale: 是否使用对数尺度显示频谱
    
    Returns:
        spectrum: 可视化的频谱图像
    """
    # 如果是彩色图像，转换为灰度
    if len(image.shape) == 3:
        gray = np.mean(image, axis=2)
    else:
        gray = image
    
    # 执行傅里叶变换并将零频率移到中心
    f_transform = fft.fft2(gray)
    f_shift = fft.fftshift(f_transform)
    
    # 计算频谱幅度
    magnitude_spectrum = np.abs(f_shift)
    
    # 对数尺度显示（可选）
    if log_scale:
        # 添加一个小值避免log(0)
        magnitude_spectrum = np.log1p(magnitude_spectrum)
    
    # 归一化到[0, 1]范围便于显示
    spectrum = (magnitude_spectrum - magnitude_spectrum.min()) / (magnitude_spectrum.max() - magnitude_spectrum.min())
    
    return spectrum

def process_image_with_multiple_ratios(image_path: str, ratios: list) -> dict:
    """
    使用多个保留比例处理图像并计算PSNR
    
    Args:
        image_path: 图像文件路径
        ratios: 保留比例列表
    
    Returns:
        results: 包含压缩图像和PSNR的字典
    """
    # 加载图像
    original_image = load_image(image_path)
    
    results = {}
    for ratio in ratios:
        # 压缩图像
        compressed_image = compress_frequency(original_image, ratio)
        
        # 计算PSNR
        psnr = compute_psnr(original_image, compressed_image)
        
        # 存储结果
        results[ratio] = {
            'compressed_image': compressed_image,
            'psnr': psnr
        }
    
    return results

def visualize_comparison(original_image, compressed_results, results_dir=None):
    """
    可视化不同保留比例下的压缩结果并比较
    
    Args:
        original_image: 原始图像
        compressed_results: 包含不同保留比例下压缩图像和PSNR的字典
        results_dir: 结果保存目录，如果提供则保存图像
    """
    # 获取保留比例列表，按顺序排序
    ratios = sorted(compressed_results.keys())
    n_ratios = len(ratios)
    
    # 创建一个大图，包含原始图像和所有压缩图像
    plt.figure(figsize=(15, 10))
    
    # 创建网格布局
    gs = gridspec.GridSpec(2, n_ratios + 1)
    
    # 显示原始图像
    ax = plt.subplot(gs[0, 0])
    ax.imshow(original_image)
    ax.set_title('Original Image')
    ax.axis('off')
    
    # 显示不同保留比例下的压缩图像
    for i, ratio in enumerate(ratios):
        result = compressed_results[ratio]
        compressed_img = result['compressed_image']
        psnr = result['psnr']
        
        ax = plt.subplot(gs[0, i+1])
        ax.imshow(compressed_img)
        ax.set_title(f'Retention Ratio: {ratio:.1f}\nPSNR: {psnr:.2f} dB')
        ax.axis('off')
    
    # 显示原始图像的频谱
    ax = plt.subplot(gs[1, 0])
    original_spectrum = visualize_spectrum(original_image)
    ax.imshow(original_spectrum, cmap='viridis')
    ax.set_title('Original Spectrum')
    ax.axis('off')
    
    # 显示不同保留比例下的频谱
    for i, ratio in enumerate(ratios):
        result = compressed_results[ratio]
        compressed_img = result['compressed_image']
        
        ax = plt.subplot(gs[1, i+1])
        compressed_spectrum = visualize_spectrum(compressed_img)
        ax.imshow(compressed_spectrum, cmap='viridis')
        ax.set_title(f'Ratio: {ratio:.1f} Spectrum')
        ax.axis('off')
    
    plt.tight_layout()
    
    # 如果提供了保存目录，则保存图像
    if results_dir:
        import os
        plt.savefig(os.path.join(results_dir, 'comparison.png'), dpi=300, bbox_inches='tight')
    
    plt.show()

def visualize_psnr_plot(compressed_results):
    """
    绘制PSNR与保留比例的关系图
    
    Args:
        compressed_results: 包含不同保留比例下压缩图像和PSNR的字典
    """
    # 获取保留比例和对应的PSNR值
    ratios = []
    psnr_values = []
    
    for ratio in sorted(compressed_results.keys()):
        ratios.append(ratio)
        psnr_values.append(compressed_results[ratio]['psnr'])
    
    # 绘制PSNR-保留比例曲线
    plt.figure(figsize=(10, 6))
    plt.plot(ratios, psnr_values, 'o-', linewidth=2, markersize=8)
    plt.grid(True, linestyle='--', alpha=0.7)
    plt.xlabel('Retention Ratio')
    plt.ylabel('PSNR (dB)')
    plt.title('PSNR vs Retention Ratio')
    
    # 添加数据标签
    for i, (x, y) in enumerate(zip(ratios, psnr_values)):
        plt.annotate(f'{y:.2f} dB', 
                    (x, y), 
                    textcoords="offset points",
                    xytext=(0, 10), 
                    ha='center')
    
    plt.tight_layout()
    plt.show()

def calculate_compression_ratio(original_size, retention_ratio):
    """
    计算理论压缩比
    
    Args:
        original_size: 原始图像大小
        retention_ratio: 保留比例
    
    Returns:
        compression_ratio: 压缩比
    """
    # 估计保留低频成分所需的空间
    # 假设低频成分占用的空间与保留比例的平方成正比
    return 1 / (retention_ratio ** 2)

def main():
    """
    主函数：演示频率压缩算法的使用
    """
    # 设定保留比例
    retention_ratios = [0.1, 0.3, 0.5, 0.7]
    
    # 导入所需库
    import os
    import sys
    from pathlib import Path
    
    # 获取项目根目录
    try:
        # 尝试使用__file__变量(在正常Python脚本中有效)
        ROOT = Path(__file__).resolve().parent.parent  # ../..
    except NameError:
        # 在Jupyter Notebook中__file__未定义，使用替代方法
        try:
            # 假设当前工作目录是项目根目录或在其下级目录中
            current_dir = os.getcwd()
            if "proj1_code" in current_dir:
                # 如果在proj1_code目录中，向上一级
                ROOT = Path(current_dir).parent
            else:
                # 否则假设已经在项目根目录
                ROOT = Path(current_dir)
        except:
            # 最后的备选方案：直接指定路径
            print("警告：无法自动确定项目根目录，使用默认路径")
            ROOT = Path(".")
    
    print(f"使用项目根目录: {ROOT}")
    
    # 设置图像路径，尝试多个可能的位置
    possible_paths = [
        os.path.join(ROOT, "data", "1a_dog.bmp"),
        os.path.join(ROOT, "proj1_code", "data", "1a_dog.bmp"),
        os.path.join(".", "data", "1a_dog.bmp"),
        os.path.join("..", "data", "1a_dog.bmp")
    ]
    
    image_path = None
    for path in possible_paths:
        if os.path.exists(path):
            image_path = path
            print(f"找到图像: {path}")
            break
    
    if image_path is None:
        print("错误：找不到图像文件。请检查data目录是否存在且包含1a_dog.bmp")
        return
    
    # 加载图像
    original_image = load_image(image_path)
    
    # 为结果创建目录
    try:
        results_dir = os.path.join(ROOT, "results", "part4")
        os.makedirs(results_dir, exist_ok=True)
    except:
        results_dir = os.path.join(".", "results", "part4")
        os.makedirs(results_dir, exist_ok=True)
    
    print(f"结果将保存至: {results_dir}")
    
    # 保存原始图像
    save_image(os.path.join(results_dir, "original.jpg"), original_image)
    
    # 处理和保存频谱图
    spectrum = visualize_spectrum(original_image)
    save_image(os.path.join(results_dir, "spectrum.jpg"), spectrum)
    
    print("\n处理图像使用不同保留比例...")
    print("保留比例\tPSNR (dB)\t估计压缩比")
    print("-" * 40)
    
    # 存储不同保留比例的结果
    compressed_results = {}
    
    # 对每个保留比例进行处理
    for ratio in retention_ratios:
        # 压缩图像
        compressed_image = compress_frequency(original_image, ratio)
        
        # 计算PSNR
        psnr = compute_psnr(original_image, compressed_image)
        
        # 估计压缩比
        comp_ratio = calculate_compression_ratio(original_image.size, ratio)
        
        print(f"{ratio:.1f}\t\t{psnr:.2f}\t\t{comp_ratio:.1f}:1")
        
        # 保存压缩图像
        save_image(os.path.join(results_dir, f"compressed_{ratio:.1f}.jpg"), compressed_image)
        
        # 保存频谱图
        compressed_spectrum = visualize_spectrum(compressed_image)
        save_image(os.path.join(results_dir, f"spectrum_{ratio:.1f}.jpg"), compressed_spectrum)
        
        # 存储结果
        compressed_results[ratio] = {
            'compressed_image': compressed_image,
            'psnr': psnr
        }
    
    print("\n完成！压缩图像和频谱已保存到目录: " + results_dir)
    
    # 可视化比较不同保留比例的结果
    visualize_comparison(original_image, compressed_results, results_dir)
    
    # 绘制PSNR与保留比例的关系图
    visualize_psnr_plot(compressed_results)

if __name__ == "__main__":
    main()