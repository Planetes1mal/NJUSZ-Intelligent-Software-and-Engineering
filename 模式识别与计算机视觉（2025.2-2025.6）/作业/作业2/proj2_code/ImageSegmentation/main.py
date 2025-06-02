import os
import glob
import argparse
from kmeans import save_segmentation_results
from region_growth import save_region_growing_results

def main():
    # 解析命令行参数
    parser = argparse.ArgumentParser(description='图像分割算法')
    parser.add_argument('--mode', type=str, default='all', 
                        choices=['kmeans', 'region_growth', 'all'],
                        help='选择运行的算法: kmeans, region_growth, 或 all')
    args = parser.parse_args()
    
    # 输入和输出目录
    input_dir = 'data'
    kmeans_output_dir = 'output/k-means'
    region_growth_output_dir = 'output/Region-Growth'
    
    # 获取所有图像文件
    image_files = glob.glob(os.path.join(input_dir, '*.jpg')) + \
                 glob.glob(os.path.join(input_dir, '*.png')) + \
                 glob.glob(os.path.join(input_dir, '*.jpeg'))
    
    # 运行K-means图像分割
    if args.mode == 'kmeans' or args.mode == 'all':
        # 确保输出目录存在
        if not os.path.exists(kmeans_output_dir):
            os.makedirs(kmeans_output_dir)
            
        k_values = [2, 4, 8, 16, 64]
        max_iters = 100
        epsilon = 1.0
        
        print("=" * 50)
        print("开始K-means图像分割...")
        for image_path in image_files:
            print(f"正在处理图像: {image_path}")
            save_segmentation_results(image_path, kmeans_output_dir, k_values, max_iters, epsilon)
    
    # 运行区域生长图像分割
    if args.mode == 'region_growth' or args.mode == 'all':
        # 确保输出目录存在
        if not os.path.exists(region_growth_output_dir):
            os.makedirs(region_growth_output_dir)
            
        n_seeds = 25  # 减少种子点数量提高性能
        
        # 为每个准则设置不同的阈值
        thresholds = {
            'abs_diff': 15,       # 绝对差异阈值
            'mean_diff': 20,      # 均值差异阈值
            'color_diff': 40,     # 颜色差异阈值
            'texture': 0.35       # 纹理差异阈值
        }
        
        # 区域生长准则列表
        criteria = ['abs_diff', 'mean_diff', 'color_diff', 'texture']
        
        print("=" * 50)
        print("开始区域生长图像分割...")
        for image_path in image_files:
            print(f"正在处理图像: {image_path}")
            save_region_growing_results(image_path, region_growth_output_dir, n_seeds, thresholds, criteria)

if __name__ == "__main__":
    main() 