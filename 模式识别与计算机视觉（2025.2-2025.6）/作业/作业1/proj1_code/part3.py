#!/usr/bin/python3

"""
PyTorch tutorial on constructing neural networks:
https://pytorch.org/tutorials/beginner/blitz/neural_networks_tutorial.html
"""

import torch
import torch.nn as nn
import torch.nn.functional as F


def my_conv2d_pytorch(image: torch.Tensor, kernel: torch.Tensor) -> torch.Tensor:
    """
    Applies input filter(s) to the input image.

    Args:
        image: Tensor of shape (1, d1, h1, w1)
        kernel: Tensor of shape (N, d1/groups, k, k) to be applied to the image
    Returns:
        filtered_image: Tensor of shape (1, d2, h2, w2) where
           d2 = N
           h2 = (h1 - k + 2 * padding) / stride + 1
           w2 = (w1 - k + 2 * padding) / stride + 1

    HINTS:
    - You should use the 2d convolution operator from torch.nn.functional.
    - In PyTorch, d1 is `in_channels`, and d2 is `out_channels`
    - Make sure to pad the image appropriately (it's a parameter to the
      convolution function you should use here!).
    - You can assume the number of groups is equal to the number of input channels.
    - You can assume only square filters for this function.
    """

    ############################
    ### TODO: YOUR CODE HERE ###

    # 获取图像的维度信息
    batch_size, in_channels, h1, w1 = image.shape
    
    # 获取卷积核的维度信息
    out_channels, in_channels_per_group, k, _ = kernel.shape  # k是卷积核的大小
    
    # 计算分组数 - 输入通道数除以每组使用的输入通道数
    groups = in_channels // in_channels_per_group
    
    # 计算填充大小，使输出尺寸满足公式要求
    # 为了保持尺寸不变，填充应为卷积核大小的一半（向下取整）
    padding = k // 2
    
    # 设置步长为1
    stride = 1
    
    # 使用torch.nn.functional.conv2d应用卷积
    filtered_image = F.conv2d(
        input=image,           # 输入图像
        weight=kernel,         # 卷积核
        bias=None,             # 不使用偏置
        stride=stride,         # 步长
        padding=padding,       # 填充
        groups=groups          # 分组卷积
    )

    ### END OF STUDENT CODE ####
    ############################

    return filtered_image
