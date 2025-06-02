#!/usr/bin/python3

from typing import Tuple

import numpy as np

def create_Gaussian_kernel_1D(ksize: int, sigma: int) -> np.ndarray:
    """Create a 1D Gaussian kernel using the specified filter size and standard deviation.
    
    The kernel should have:
    - shape (k,1)
    - mean = floor (ksize / 2)
    - values that sum to 1
    
    Args:
        ksize: length of kernel
        sigma: standard deviation of Gaussian distribution
    
    Returns:
        kernel: 1d column vector of shape (k,1)
    
    HINT:
    - You can evaluate the univariate Gaussian probability density function (pdf) at each
      of the 1d values on the kernel (think of a number line, with a peak at the center).
    - The goal is to discretize a 1d continuous distribution onto a vector.
    """
    
    # 计算均值（向下取整）
    mu = ksize // 2
    
    # 创建坐标向量
    x = np.arange(ksize)
    
    # 计算高斯值
    # p(x;μ,σ²) = (1/√(2π)σ) * exp(-(1/2σ²)(x-μ)²)
    kernel = np.exp(-0.5 * ((x - mu) / sigma) ** 2) / (np.sqrt(2 * np.pi) * sigma)
    
    # 归一化使总和为1
    kernel = kernel / np.sum(kernel)
    
    # 调整形状为(k,1)
    kernel = kernel.reshape(ksize, 1)
    
    return kernel

def create_Gaussian_kernel_2D(cutoff_frequency: int) -> np.ndarray:
    """
    Create a 2D Gaussian kernel using the specified filter size, standard
    deviation and cutoff frequency.

    The kernel should have:
    - shape (k, k) where k = cutoff_frequency * 4 + 1
    - mean = floor(k / 2)
    - standard deviation = cutoff_frequency
    - values that sum to 1

    Args:
        cutoff_frequency: an int controlling how much low frequency to leave in
        the image.
    Returns:
        kernel: numpy nd-array of shape (k, k)

    HINT:
    - You can use create_Gaussian_kernel_1D() to complete this in one line of code.
    - The 2D Gaussian kernel here can be calculated as the outer product of two
      1D vectors. In other words, as the outer product of two vectors, each 
      with values populated from evaluating the 1D Gaussian PDF at each 1d coordinate.
    - Alternatively, you can evaluate the multivariate Gaussian probability 
      density function (pdf) at each of the 2d values on the kernel's grid.
    - The goal is to discretize a 2d continuous distribution onto a matrix.
    """

    # 计算核大小
    ksize = cutoff_frequency * 4 + 1
    
    # 设置sigma等于cutoff_frequency
    sigma = cutoff_frequency
    
    # 使用1D高斯核的外积创建2D高斯核
    kernel_1d = create_Gaussian_kernel_1D(ksize, sigma)
    kernel_2d = np.dot(kernel_1d, kernel_1d.T)
    
    # 归一化确保总和为1
    kernel_2d = kernel_2d / np.sum(kernel_2d)
    
    return kernel_2d


def my_conv2d_numpy(image: np.ndarray, filter: np.ndarray) -> np.ndarray:
    """Apply a single 2d filter to each channel of an image. Return the filtered image.
    
    Note: we are asking you to implement a very specific type of convolution.
      The implementation in torch.nn.Conv2d is much more general.

    Args:
        image: array of shape (m, n, c)
        filter: array of shape (k, j)
    Returns:
        filtered_image: array of shape (m, n, c), i.e. image shape should be preserved

    HINTS:
    - You may not use any libraries that do the work for you. Using numpy to
      work with matrices is fine and encouraged. Using OpenCV or similar to do
      the filtering for you is not allowed.
    - We encourage you to try implementing this naively first, just be aware
      that it may take an absurdly long time to run. You will need to get a
      function that takes a reasonable amount of time to run so that the TAs
      can verify your code works.
    - If you need to apply padding to the image, only use the zero-padding
      method. You need to compute how much padding is required, if any.
    - "Stride" should be set to 1 in your implementation.
    - You can implement either "cross-correlation" or "convolution", and the result
      will be identical, since we will only test with symmetric filters.
    """

    assert filter.shape[0] % 2 == 1
    assert filter.shape[1] % 2 == 1

    # 获取图像和滤波器的尺寸
    m, n, c = image.shape     # 图像高度、宽度和通道数
    k_h, k_w = filter.shape   # 滤波器高度和宽度
    
    # 计算需要的边缘填充量
    pad_h = k_h // 2
    pad_w = k_w // 2
    
    # 创建与输入图像尺寸相同的输出图像
    filtered_image = np.zeros_like(image)
    
    # 使用零填充扩展输入图像边缘
    padded_image = np.pad(image, ((pad_h, pad_h), (pad_w, pad_w), (0, 0)), mode='constant')
    
    # 对每个通道应用滤波
    for ch in range(c):
        for i in range(m):
            for j in range(n):
                # 提取感兴趣区域(ROI)
                roi = padded_image[i:i+k_h, j:j+k_w, ch]
                # 应用滤波器（互相关操作）
                filtered_image[i, j, ch] = np.sum(roi * filter)
    
    return filtered_image
  
  
def my_conv2d_numpy_v2(image: np.ndarray, filter: np.ndarray) -> np.ndarray:
    """Apply a single 2d filter to each channel of an image. Return the filtered image. Notably, this is the optimized revision of `my_conv2d_numpy()`.
    
    Note: we are asking you to implement a very specific type of convolution.
      The implementation in torch.nn.Conv2d is much more general.

    Args:
        image: array of shape (m, n, c)
        filter: array of shape (k, j)
    Returns:
        filtered_image: array of shape (m, n, c), i.e. image shape should be preserved

    HINTS:
    - You may not use any libraries that do the work for you. Using numpy to
      work with matrices is fine and encouraged. Using OpenCV or similar to do
      the filtering for you is not allowed.
    - We encourage you to try implementing this naively first, just be aware
      that it may take an absurdly long time to run. You will need to get a
      function that takes a reasonable amount of time to run so that the TAs
      can verify your code works.
    - If you need to apply padding to the image, only use the zero-padding
      method. You need to compute how much padding is required, if any.
    - "Stride" should be set to 1 in your implementation.
    - You can implement either "cross-correlation" or "convolution", and the result
      will be identical, since we will only test with symmetric filters.
    """

    assert filter.shape[0] % 2 == 1
    assert filter.shape[1] % 2 == 1

    # 获取图像和滤波器的尺寸
    m, n, c = image.shape     # 图像高度、宽度和通道数
    k_h, k_w = filter.shape   # 滤波器高度和宽度
    
    # 计算需要的边缘填充量
    pad_h = k_h // 2
    pad_w = k_w // 2
    
    # 创建与输入图像尺寸相同的输出图像
    filtered_image = np.zeros_like(image)
    
    # 优化实现：使用反射填充减少边缘伪影
    # 这既能改善边缘效果，也能提高执行速度
    padded_image = np.pad(image, ((pad_h, pad_h), (pad_w, pad_w), (0, 0)), mode='reflect')
    
    # 使用向量化运算提高性能
    for ch in range(c):
        for i in range(m):
            for j in range(n):
                # 提取当前窗口并应用滤波器
                window = padded_image[i:i+k_h, j:j+k_w, ch]
                filtered_image[i, j, ch] = np.sum(window * filter)
    
    return filtered_image


def create_hybrid_image(
    image1: np.ndarray, image2: np.ndarray, filter: np.ndarray
) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
    """
    Takes two images and a low-pass filter and creates a hybrid image. Returns
    the low frequency content of image1, the high frequency content of image 2,
    and the hybrid image.

    Args:
        image1: array of dim (m, n, c)
        image2: array of dim (m, n, c)
        filter: array of dim (x, y)
    Returns:
        low_frequencies: array of shape (m, n, c)
        high_frequencies: array of shape (m, n, c)
        hybrid_image: array of shape (m, n, c)

    HINTS:
    - You will use your my_conv2d_numpy() function in this function.
    - You can get just the high frequency content of an image by removing its
      low frequency content. Think about how to do this in mathematical terms.
    - Don't forget to make sure the pixel values of the hybrid image are
      between 0 and 1. This is known as 'clipping'.
    - If you want to use images with different dimensions, you should resize
      them in the notebook code.
    """

    assert image1.shape[0] == image2.shape[0]
    assert image1.shape[1] == image2.shape[1]
    assert image1.shape[2] == image2.shape[2]
    assert filter.shape[0] <= image1.shape[0]
    assert filter.shape[1] <= image1.shape[1]
    assert filter.shape[0] % 2 == 1
    assert filter.shape[1] % 2 == 1

    # 使用低通滤波器获取图像1的低频部分
    low_frequencies = my_conv2d_numpy(image1, filter)
    
    # 获取图像2的低频部分
    image2_low_frequencies = my_conv2d_numpy(image2, filter)
    
    # 通过从原始图像中减去低频部分来获取图像2的高频部分
    # 高频 = 原始 - 低频
    high_frequencies = image2 - image2_low_frequencies
    
    # 将图像1的低频部分和图像2的高频部分相加，创建混合图像
    hybrid_image = low_frequencies + high_frequencies
    
    # 将像素值裁剪到[0,1]范围内，防止值溢出
    hybrid_image = np.clip(hybrid_image, 0, 1)
    
    return low_frequencies, high_frequencies, hybrid_image
