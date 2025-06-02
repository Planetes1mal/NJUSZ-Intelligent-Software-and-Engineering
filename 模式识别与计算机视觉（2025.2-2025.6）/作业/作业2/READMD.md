# 模式识别与计算机视觉项目2

## 项目概述

本项目实现了两类计算机视觉的基础算法：边缘检测和图像分割。每类算法都包含多种不同实现方法，主要内容包括：

1. **边缘检测算法**：
   - 拉普拉斯算子：基于二阶导数的边缘检测
   - 索贝尔算子：基于一阶导数的边缘检测
   - Canny 算法：多阶段的高级边缘检测

2. **图像分割算法**：
   - 基于 K-means 聚类的图像分割
   - 基于区域生长的图像分割（实现四种不同的生长准则）

## 环境配置

```bash
conda create -n cv_proj2 python=3.8 -y
conda activate cv_proj2
pip install numpy opencv-python matplotlib
```

## 项目结构

```
proj2_code/
├── EdgeDetection/          # 边缘检测模块
│   ├── data/               # 输入图像（BSDS数据集）
│   ├── output/             # 结果输出目录
│   │   ├── Laplacian/      # 拉普拉斯算子结果
│   │   ├── Sobel/          # 索贝尔算子结果
│   │   └── Canny/          # Canny算子结果
│   ├── laplacian.py        # 自定义拉普拉斯实现
│   ├── sobel.py            # 自定义索贝尔实现
│   ├── canny.py            # 自定义Canny实现
│   └── main.py             # 边缘检测主程序
├── ImageSegmentation/      # 图像分割模块
│   ├── data/               # 输入图像
│   ├── output/             # 结果输出目录
│   │   ├── k-means/        # K-means分割结果
│   │   └── Region-Growth/  # 区域生长分割结果
│   ├── kmeans.py           # K-means聚类实现
│   ├── region_growth.py    # 区域生长实现
│   └── main.py             # 图像分割主程序
└── README.md               # 项目说明文件
```

## 如何运行代码

### 边缘检测

```bash
cd proj2_code/EdgeDetection
```

**拉普拉斯算子**

```bash
python main.py --mode laplacian --input_dir data/ --output_dir output/laplacian
```

**索贝尔算子**

```bash
python main.py --mode sobel --input_dir data/ --output_dir output/sobel
```

**Canny 算子**

```bash
python main.py --mode canny --input_dir data/ --output_dir output/canny --lowThreshold 50 --highThreshold 150
```

### 图像分割

```bash
cd proj2_code/ImageSegmentation
```

**运行全部分割算法**

```bash
python main.py
```

**K-means 聚类分割**

```bash
python main.py --mode kmeans
```

**区域生长分割**

```bash
python main.py --mode region_growth
```





