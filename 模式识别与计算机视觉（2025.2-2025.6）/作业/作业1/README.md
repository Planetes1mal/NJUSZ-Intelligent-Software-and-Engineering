# 模式识别与计算机视觉项目1

本项目实现了图像频率处理的相关算法，包括混合图像生成、频率压缩等功能。项目主要分为四个部分：

1. 基础混合图像算法（NumPy实现）
2. 使用PyTorch实现的混合图像处理
3. 卷积操作的PyTorch实现
4. 图像频率压缩算法

## 环境配置

1. **安装Anaconda**：

   1. 访问Anaconda官网

   2. 选择 **Windows 64-Bit Graphical Installer** 下载

   3. 双击安装包运行安装程序：

      - 安装路径建议保持默认（如 `C:\Users\<用户名>\Anaconda3`）

      - 勾选 **"Add Anaconda3 to my PATH environment variable"**（环境变量配置）

      - 完成安装后，在开始菜单中搜索并打开 **Anaconda Prompt**

2. **下载和解压项目代码**

   1. 下载 `proj1.zip` 文件
   2. 右键解压到目标文件夹

   确保路径不含中文或空格（避免后续路径错误）

3. **创建Conda环境**

   1. 在 Anaconda Prompt 中切换至项目目录：

      ```bash
      cd C:\CV_Project
      ```

   2. 根据Windows系统执行环境配置命令：

      ```bash
      conda env create -f proj1_env_win.yml
      ```

4. 激活环境

   ```bash
   conda activate cv_proj1
   ```

   激活后命令提示符前会显示 `(cv_proj1)`

5. **安装项目依赖包**

   1. 确保当前目录为项目根目录（包含 `setup.py` 文件）

   2. 执行以下命令：

      ```bash
      pip install -e .
      ```


6. **运行单元测试**：在 Anaconda Prompt 中执行：

   ```
   pytest proj1_unit_tests
   ```



## 项目结构

```
├── data/                  # 存放输入图像
├── results/               # 存放生成的结果
├── proj1_code/            # 主要代码目录
│   ├── proj1.ipynb		   
│   ├── part1.py           # 基础混合图像算法（NumPy实现）
│   ├── part2_datasets.py  # 数据集加载类
│   ├── part2_models.py    # PyTorch模型实现
│   ├── part3.py           # 卷积操作的PyTorch实现
│   ├── part4.py           # 图像频率压缩算法
│   ├── custom_hybrid_images.py          # 自定义混合图像生成（NumPy版本）
│   ├── custom_hybrid_images_pytorch.py  # 自定义混合图像生成（PyTorch版本）
│   └── utils.py           # 工具函数
└── proj1_unit_tests/      # 单元测试
```

## 如何运行

### Part 1：基础混合图像算法

Part 1 实现了使用NumPy创建混合图像的基本算法，包括高斯核生成、图像卷积和混合图像生成。

```bash
python -m proj1_code.part1
```

要测试Part 1的实现：

```bash
python -m proj1_unit_tests.test_part1
```

### Part 2：PyTorch实现的混合图像处理

Part 2 提供了使用PyTorch实现的混合图像处理，包括数据集加载和模型实现。

```bash
# 运行自定义混合图像生成（PyTorch版本）
python -m proj1_code.custom_hybrid_images_pytorch
```

要测试Part 2的实现：

```bash
python -m proj1_unit_tests.test_part2
```

### Part 3：卷积操作的PyTorch实现

Part 3 实现了使用PyTorch的卷积操作。

要测试Part 3的实现：

```bash
python -m proj1_unit_tests.test_part3
```

### Part 4：图像频率压缩算法

Part 4 实现了图像频率压缩算法，可以使用不同的保留比例对图像进行压缩，并计算PSNR值。

```bash
python -m proj1_code.part4
```

这将生成不同保留比例下的压缩图像，并显示图像比较和PSNR曲线。结果将保存在`results/part4/`目录中。

## 使用自定义图像

要使用自定义图像生成混合图像，可以将图像放入`data/`目录，然后相应修改`custom_hybrid_images.py`或`custom_hybrid_images_pytorch.py`文件中的图像路径和截止频率。

## 各模块详细说明

### Part 1：基础混合图像算法 (part1.py)

这部分实现了：
- 创建1D和2D高斯核
- 使用NumPy实现卷积操作
- 创建混合图像

```python
# 示例：创建混合图像
import numpy as np
from proj1_code.part1 import create_Gaussian_kernel_2D, create_hybrid_image
from proj1_code.utils import load_image

# 加载图像
image1 = load_image("data/1a_dog.bmp")
image2 = load_image("data/1b_cat.bmp")

# 创建高斯核
kernel = create_Gaussian_kernel_2D(7)  # 截止频率为7

# 创建混合图像
low_freqs, high_freqs, hybrid = create_hybrid_image(image1, image2, kernel)
```

### Part 2：数据集和模型 (part2_datasets.py, part2_models.py)

这部分实现了：
- 加载图像对的数据集
- 读取截止频率
- 使用PyTorch实现混合图像模型

```python
# 示例：使用PyTorch模型创建混合图像
import torch
from proj1_code.part2_models import HybridImageModel
from torchvision import transforms
from PIL import Image

# 加载图像
transform = transforms.ToTensor()
image1 = transform(Image.open("data/1a_dog.bmp")).unsqueeze(0)
image2 = transform(Image.open("data/1b_cat.bmp")).unsqueeze(0)
cutoff_frequency = torch.Tensor([7])

# 创建模型
model = HybridImageModel()

# 生成混合图像
low_freqs, high_freqs, hybrid = model(image1, image2, cutoff_frequency)
```

### Part 3：PyTorch卷积实现 (part3.py)

这部分实现了使用PyTorch的函数式API进行2D卷积操作。

```python
# 示例：使用PyTorch进行卷积
import torch
from proj1_code.part3 import my_conv2d_pytorch

# 创建示例图像和卷积核
image = torch.randn(1, 3, 32, 32)  # 批量大小=1, 通道=3, 高度=宽度=32
kernel = torch.randn(6, 1, 5, 5)  # 输出通道=6, 每组输入通道=1, 高度=宽度=5

# 应用卷积
filtered_image = my_conv2d_pytorch(image, kernel)
```

### Part 4：图像频率压缩 (part4.py)

这部分实现了：
- 使用傅里叶变换进行图像频率压缩
- 计算PSNR
- 可视化压缩结果

```python
# 示例：压缩图像
from proj1_code.part4 import compress_frequency, compute_psnr
from proj1_code.utils import load_image

# 加载图像
image = load_image("data/1a_dog.bmp")

# 使用不同保留比例压缩图像
retention_ratio = 0.3
compressed_image = compress_frequency(image, retention_ratio)

# 计算PSNR
psnr = compute_psnr(image, compressed_image)
print(f"PSNR: {psnr:.2f} dB")
```

## 运行多图像处理

要处理多个图像对并比较NumPy和PyTorch实现的性能：

```bash
# NumPy实现
python -m proj1_code.custom_hybrid_images

# PyTorch实现
python -m proj1_code.custom_hybrid_images_pytorch
```

## 问题排查

如果遇到图像路径相关的错误，请确保：
1. 数据目录结构正确
2. 图像文件存在于指定路径
3. 有正确的读取权限

如果在Jupyter Notebook中运行遇到`__file__`相关错误，请使用项目中提供的替代方法确定路径：

```python
import os
from pathlib import Path

# 获取项目根目录
try:
    # 尝试使用__file__变量(在正常Python脚本中有效)
    ROOT = Path(__file__).resolve().parent.parent
except NameError:
    # 在Jupyter Notebook中__file__未定义，使用替代方法
    current_dir = os.getcwd()
    if "proj1_code" in current_dir:
        ROOT = Path(current_dir).parent
    else:
        ROOT = Path(current_dir)
```

## 可视化结果

所有生成的图像和可视化结果将保存在`results/`目录中，按照不同部分组织：
- `results/part1/`：基础混合图像结果
- `results/part1_custom/`：自定义混合图像结果（NumPy版本）
- `results/part2_custom/`：自定义混合图像结果（PyTorch版本）
- `results/part4/`：图像频率压缩结果
