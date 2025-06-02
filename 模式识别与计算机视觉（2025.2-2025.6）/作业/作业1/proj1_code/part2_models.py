#!/usr/bin/python3

"""
PyTorch tutorial on constructing neural networks:
https://pytorch.org/tutorials/beginner/blitz/neural_networks_tutorial.html
"""
import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F

from proj1_code.part1 import create_Gaussian_kernel_2D


class HybridImageModel(nn.Module):
    def __init__(self):
        """
        初始化HybridImageModel类的实例。
        """
        super().__init__()  # 修复super()调用

    def get_kernel(self, cutoff_frequency: int) -> torch.Tensor:
        """
        使用指定的截止频率返回高斯核。

        PyTorch要求核必须具有特定形状才能将其应用于图像。具体来说，核需要是形状
        (c, 1, k, k)，其中c是图像中的通道数。首先获取Part 1中实现的2D高斯核，
        它的形状为(k, k)。然后，假设你有一个RGB图像，你需要将这个高斯核堆叠3次，
        将其转换为形状为(3, 1, k, k)的张量。

        Args
            cutoff_frequency: 指定截止频率的整数
        Returns
            kernel: 形状为(c, 1, k, k)的张量，其中c是通道数

        提示:
        - 你将在此函数中使用part1.py中的create_Gaussian_kernel_2D()函数。
        - 由于数据集中的每个图像的通道数可能不同，确保你不要硬编码重塑核的维度。
        - 在这个类中有一个变量可以为你提供通道信息。
        - 你可以使用np.reshape()来改变numpy数组的维度。
        - 你可以使用np.tile()沿指定轴重复numpy数组。
        - 你可以使用torch.Tensor()将numpy数组转换为torch张量。
        """

        ############################
        ### TODO: YOUR CODE HERE ###

        # 确保cutoff_frequency是整数
        cutoff_frequency = int(cutoff_frequency)
        
        # 创建二维高斯核 - 形状为(k, k)
        kernel_2d = create_Gaussian_kernel_2D(cutoff_frequency)
        
        # 获取当前实例中设置的通道数
        n_channels = self.n_channels
        
        # 重塑核，首先添加一个维度，使其形状为(1, k, k)
        kernel_reshaped = kernel_2d.reshape(1, kernel_2d.shape[0], kernel_2d.shape[1])
        
        # 沿第一个维度复制，使其形状为(c, 1, k, k)
        kernel_final = np.tile(kernel_reshaped, (n_channels, 1, 1, 1))
        
        # 将numpy数组转换为torch张量
        kernel = torch.Tensor(kernel_final)

        ### END OF STUDENT CODE ####
        ############################

        return kernel

    def low_pass(self, x: torch.Tensor, kernel: torch.Tensor):
        """
        对输入图像应用低通滤波器。

        Args:
            x: 形状为(b, c, m, n)的张量，其中b是批量大小
            kernel: 应用于图像的低通滤波器
        Returns:
            filtered_image: 形状为(b, c, m, n)的张量

        提示:
        - 你应该使用torch.nn.functional中的2d卷积算子。
        - 确保适当地填充图像（这是你应该在这里使用的卷积函数的参数！）。
        - 将self.n_channels作为卷积函数的"groups"参数的值。这表示滤波器将应用的通道数。
        """

        ############################
        ### TODO: YOUR CODE HERE ###

        # 获取kernel的大小
        kernel_size = kernel.shape[2]
        
        # 计算填充大小 - 这使输出大小与输入大小相同
        # 当kernel_size是奇数时，padding = (kernel_size - 1) / 2
        padding = (kernel_size - 1) // 2
        
        # 使用torch.nn.functional.conv2d应用滤波器
        # groups=self.n_channels表示每个输入通道将与各自的滤波器单独卷积
        filtered_image = F.conv2d(
            input=x,              # 输入图像
            weight=kernel,        # 卷积核
            padding=padding,      # 填充大小
            groups=self.n_channels # 组数等于通道数
        )

        ### END OF STUDENT CODE ####
        ############################

        return filtered_image

    def forward(
        self, image1: torch.Tensor, image2: torch.Tensor, cutoff_frequency: torch.Tensor
    ):
        """
        接受两个图像并创建一个混合图像。返回image1的低频内容，
        image2的高频内容，以及混合图像。

        Args:
            image1: 形状为(b, c, m, n)的张量
            image2: 形状为(b, c, m, n)的张量
            cutoff_frequency: 形状为(b)的张量
        Returns:
            low_frequencies: 形状为(b, c, m, n)的张量
            high_frequencies: 形状为(b, c, m, n)的张量
            hybrid_image: 形状为(b, c, m, n)的张量

        提示:
        - 你将在此函数中使用get_kernel()函数和你的low_pass()函数。
        - 与Part 1类似，你可以通过移除图像的低频内容来获取图像的高频内容。
        - 不要忘记确保像素值>=0且<=1。你可以使用torch.clamp()。
        - 如果你想使用不同维度的图像，你应该在HybridImageDataset类中使用
          torchvision.transforms调整它们的大小。
        """
        self.n_channels = image1.shape[1]

        ############################
        ### TODO: YOUR CODE HERE ###

        # 创建一个与批次大小相同大小的结果列表
        batch_size = image1.shape[0]
        low_frequencies_list = []
        high_frequencies_list = []
        hybrid_images_list = []
        
        # 对批次中的每个图像对处理
        for i in range(batch_size):
            # 获取当前图像对
            img1 = image1[i:i+1]  # 保持批次维度
            img2 = image2[i:i+1]  # 保持批次维度
            # 确保cutoff_frequency被转换为整数
            cf = cutoff_frequency[i].item()  # 获取单个浮点值
            
            # 获取核并应用低通滤波（get_kernel内部会将cf转换为int）
            kernel = self.get_kernel(cf)
            
            # 获取低频内容（第一张图像的低通滤波）
            low_freq = self.low_pass(img1, kernel)
            
            # 获取高频内容（第二张图像 - 第二张图像的低通滤波）
            low_freq_img2 = self.low_pass(img2, kernel)
            high_freq = img2 - low_freq_img2
            
            # 创建混合图像（低频 + 高频）
            hybrid = low_freq + high_freq
            
            # 确保像素值在[0,1]范围内
            hybrid = torch.clamp(hybrid, 0, 1)
            
            # 添加到结果列表
            low_frequencies_list.append(low_freq)
            high_frequencies_list.append(high_freq)
            hybrid_images_list.append(hybrid)
        
        # 组合批次结果
        low_frequencies = torch.cat(low_frequencies_list, dim=0)
        high_frequencies = torch.cat(high_frequencies_list, dim=0)
        hybrid_image = torch.cat(hybrid_images_list, dim=0)

        ### END OF STUDENT CODE ####
        ############################

        return low_frequencies, high_frequencies, hybrid_image