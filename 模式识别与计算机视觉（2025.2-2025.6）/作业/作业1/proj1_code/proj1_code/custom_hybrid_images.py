# Here we will implement the code to create custom hybrid images
# Use separate functions for each pair of images and manually specify the cutoff frequency

# Import necessary libraries
import numpy as np
import matplotlib.pyplot as plt
from proj1_code.utils import load_image, save_image, vis_image_scales_numpy
from proj1_code.part1 import create_Gaussian_kernel_2D, create_hybrid_image
import os

if not os.path.exists('../results/part1_custom/'):
    os.makedirs('../results/part1_custom/')

def create_motorcycle_bicycle_hybrid():
    print("处理图像对: 猫+狗")
    
    # 加载图像
    image1 = load_image("../data/6a_custom_cat.bmp")
    image2 = load_image("../data/6a_custom_dog.bmp")
    
    # 显示原始图像
    plt.figure(figsize=(10, 5))
    plt.subplot(1, 2, 1)
    plt.imshow((image1*255).astype(np.uint8))
    plt.title("6a_custom_cat.bmp")
    plt.subplot(1, 2, 2)
    plt.imshow((image2*255).astype(np.uint8))
    plt.title("6a_custom_dog.bmp")
    plt.tight_layout()
    plt.show()
    
    cutoff_frequency = 4
    kernel = create_Gaussian_kernel_2D(cutoff_frequency)
    low_frequencies, high_frequencies, hybrid_image = create_hybrid_image(image1, image2, kernel)
    
    # 显示结果
    plt.figure(figsize=(15, 5))
    plt.subplot(1, 3, 1)
    plt.imshow((low_frequencies*255).astype(np.uint8))
    plt.title(f"Low Frequencies (cutoff={cutoff_frequency})")
    
    plt.subplot(1, 3, 2)
    plt.imshow(((high_frequencies+0.5)*255).astype(np.uint8))
    plt.title(f"High Frequencies (cutoff={cutoff_frequency})")
    
    plt.subplot(1, 3, 3)
    plt.imshow((hybrid_image*255).astype(np.uint8))
    plt.title(f"Hybrid Image (cutoff={cutoff_frequency})")
    plt.tight_layout()
    plt.show()
    
    # 显示多尺度效果
    vis = vis_image_scales_numpy(hybrid_image)
    plt.figure(figsize=(20, 10))
    plt.imshow(vis)
    plt.title(f"Multi-scale Visualization (cutoff={cutoff_frequency})")
    plt.show()
    
    # 保存结果
    save_image('../results/part1_custom/5_low_frequencies.jpg', low_frequencies)
    save_image('../results/part1_custom/5_high_frequencies.jpg', high_frequencies+0.5)
    save_image('../results/part1_custom/5_hybrid_image.jpg', hybrid_image)
    save_image('../results/part1_custom/5_hybrid_image_scales.jpg', vis)
    
    print(f"图像对 猫+狗 的截止频率: {cutoff_frequency}")
    print("---------------------------------------------------")
    
    return cutoff_frequency

def update_cutoff_frequencies():
    cutoffs = [
        7,  # Dog + Cat
        4,  # Motorcycle + Bicycle
        8,  # Plane + Bird
        5, # Einstein + Marilyn
        4,  # Submarine + Fish
        4 # 自定义
    ]
    
    with open('../cutoff_frequencies.txt', 'w') as f:
        for cutoff in cutoffs:
            f.write(f"{cutoff}\n")
    print("cutoff_frequencies.txt file has been updated")

def main():
    motorcycle_bicycle_cutoff = create_motorcycle_bicycle_hybrid()

    update_cutoff_frequencies()
    
    print("All hybrid images have been generated with the following cutoff frequencies:")
    print("Motorcycle + Bicycle:", motorcycle_bicycle_cutoff)

if __name__ == "__main__":
    main()