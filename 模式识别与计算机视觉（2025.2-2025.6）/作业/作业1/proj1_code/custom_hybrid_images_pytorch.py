# 这里我们将实现使用 PyTorch 创建自定义混合图像的代码
# 使用单独的函数处理每对图像，并手动指定截止频率

# 导入必要的库
import numpy as np
import matplotlib.pyplot as plt
import torch
import torchvision
import os
import time
from PIL import Image
from torchvision import transforms
from proj1_code.utils import save_image
from proj1_code.part2_models import HybridImageModel

# 确保结果目录存在
if not os.path.exists('../results/part2_custom/'):
    os.makedirs('../results/part2_custom/')

def process_image_pair(image1_path, image2_path, cutoff_frequency, pair_name, index):
    """
    处理一对图像并创建混合图像
    
    Args:
        image1_path: 第一张图像的路径
        image2_path: 第二张图像的路径
        cutoff_frequency: 截止频率
        pair_name: 图像对的名称
        index: 图像对的索引
    """
    print(f"处理图像对: {pair_name}")
    
    # 加载图像并转换为 PyTorch 张量
    transform = transforms.ToTensor()
    
    # 打开并转换图像
    image1_pil = Image.open(image1_path)
    image2_pil = Image.open(image2_path)
    
    image1 = transform(image1_pil)
    image2 = transform(image2_pil)
    
    # 增加批次维度
    image1 = image1.unsqueeze(0)
    image2 = image2.unsqueeze(0)
    
    # 创建模型
    model = HybridImageModel()
    
    # 测量处理时间
    start = time.time()
    
    # 使用 PyTorch 模型生成混合图像
    low_frequencies, high_frequencies, hybrid_image = model(image1, image2, torch.Tensor([cutoff_frequency]))
    
    end = time.time() - start
    print(f'处理时间: {end:.3f} 秒')
    
    # 转换为 NumPy 数组进行显示
    low_freq_np = low_frequencies[0].permute(1, 2, 0).numpy()
    high_freq_np = high_frequencies[0].permute(1, 2, 0).numpy()
    hybrid_np = hybrid_image[0].permute(1, 2, 0).numpy()
    
    # 显示结果
    plt.figure(figsize=(15, 5))
    plt.imshow(hybrid_np)
    plt.title(f"Hybrid Image (cutoff={cutoff_frequency})")
    plt.tight_layout()
    plt.show()

    # 保存结果
    torchvision.utils.save_image(hybrid_image, f'../results/part2_custom/{index}_{pair_name}_hybrid_image.jpg')
    
    print(f"图像对 {pair_name} 的截止频率: {cutoff_frequency}")
    print("---------------------------------------------------")
    
    return cutoff_frequency

def process_all_pairs():
    # 定义图像对和截止频率
    image_pairs = [
        ("../data/1a_dog.bmp", "../data/1b_cat.bmp", 7, "狗+猫", 1),
        ("../data/2a_motorcycle.bmp", "../data/2b_bicycle.bmp", 4, "摩托车+自行车", 2),
        ("../data/3a_plane.bmp", "../data/3b_bird.bmp", 8, "飞机+鸟", 3),
        ("../data/4a_einstein.bmp", "../data/4b_marilyn.bmp", 5, "爱因斯坦+玛丽莲", 4),
        ("../data/5a_submarine.bmp", "../data/5b_fish.bmp", 4, "潜艇+鱼", 5),
        ("../data/6a_custom_cat.bmp", "../data/6a_custom_dog.bmp", 10, "自定义猫+狗", 6)
    ]
    
    cutoff_frequencies = []
    pair_names = []
    
    # 处理每对图像
    for img1_path, img2_path, cutoff, name, idx in image_pairs:
        cf = process_image_pair(img1_path, img2_path, cutoff, name, idx)
        cutoff_frequencies.append(cf)
        pair_names.append(name)
    
    # 更新截止频率文件
    with open('../cutoff_frequencies.txt', 'w') as f:
        for cutoff in cutoff_frequencies:
            f.write(f"{cutoff}\n")
    print("已更新 cutoff_frequencies.txt 文件")
    
    # 打印所有截止频率
    print("已生成所有混合图像，使用以下截止频率:")
    for name, cf in zip(pair_names, cutoff_frequencies):
        print(f"{name}: {cf}")

def compare_runtime():
    """比较 Part 1 和 Part 2 的运行时间"""
    print("\n比较 Part 1 和 Part 2 的运行时间:")
    
    # 加载图像
    image1_path = "../data/1a_dog.bmp"
    image2_path = "../data/1b_cat.bmp"
    cutoff_frequency = 7
    
    # Part 1 - NumPy 实现
    from proj1_code.utils import load_image
    from proj1_code.part1 import create_Gaussian_kernel_2D, create_hybrid_image
    
    image1_np = load_image(image1_path)
    image2_np = load_image(image2_path)
    
    from proj1_code.part1 import create_Gaussian_kernel_2D, create_hybrid_image
    
    start = time.time()
    kernel = create_Gaussian_kernel_2D(cutoff_frequency)
    low_freq_np, high_freq_np, hybrid_np = create_hybrid_image(image1_np, image2_np, kernel)
    end_np = time.time() - start
    print(f'Part 1 (NumPy 实现): {end_np:.3f} 秒')
    
    # Part 2 - PyTorch 实现
    transform = transforms.ToTensor()
    image1_pil = Image.open(image1_path)
    image2_pil = Image.open(image2_path)
    
    image1 = transform(image1_pil).unsqueeze(0)
    image2 = transform(image2_pil).unsqueeze(0)
    
    model = HybridImageModel()
    
    start = time.time()
    low_frequencies, high_frequencies, hybrid_image = model(image1, image2, torch.Tensor([cutoff_frequency]))
    end_pt = time.time() - start
    print(f'Part 2 (PyTorch 实现): {end_pt:.3f} 秒')
    
    # 计算加速比
    speed_up = (end_np / end_pt)
    print(f'加速比: {speed_up:.2f}倍')
    print(f'PyTorch 实现比 NumPy 实现快了 {(speed_up - 1) * 100:.2f}%')

def main():
    process_all_pairs()
    compare_runtime()

if __name__ == "__main__":
    main() 