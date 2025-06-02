import cv2
import numpy as np

# https://docs.opencv.org/3.4/d5/db5/tutorial_laplace_operator.html
def StandLaplacian(src, ddepth = cv2.CV_16S, kernel_size=3):
    # laplacian = cv2.Laplacian(image, cv2.CV_8U)

    # Remove noise by blurring with a Gaussian filter
    src = cv2.GaussianBlur(src, (3, 3), 0)

    # Convert the image to grayscale
    src_gray = cv2.cvtColor(src, cv2.COLOR_BGR2GRAY)

    # Apply Laplace function
    dst = cv2.Laplacian(src_gray, ddepth, ksize=kernel_size)

    # converting back to uint8
    abs_dst = cv2.convertScaleAbs(dst)

    return abs_dst

def CustomLaplacian(src, ddepth = cv2.CV_16S, kernel_size=3):
    # 去除噪声，使用高斯滤波
    src = cv2.GaussianBlur(src, (3, 3), 0)
    
    # 将图像转换为灰度图
    src_gray = cv2.cvtColor(src, cv2.COLOR_BGR2GRAY)
    
    # 创建拉普拉斯算子卷积核
    laplacian_kernel = np.array([
        [0, 3, 0],
        [3, -12, 3],
        [0, 3, 0]
    ], dtype=np.float32)
    
    # 获取图像尺寸
    rows, cols = src_gray.shape
    
    # 获取卷积核尺寸
    k_rows, k_cols = laplacian_kernel.shape
    
    # 计算填充大小（对于3x3卷积核，每边填充1像素）
    pad_size = k_rows // 2 
    
    # 对灰度图像进行边界零填充
    padded_gray = np.pad(src_gray, pad_size, mode='constant', constant_values=0)

    # 初始化用于存放卷积结果的输出图像
    if ddepth == cv2.CV_16S:
        convolved_image = np.zeros_like(src_gray, dtype=np.int16)
    elif ddepth == cv2.CV_32F:
        convolved_image = np.zeros_like(src_gray, dtype=np.float32)
    elif ddepth == cv2.CV_64F:
        convolved_image = np.zeros_like(src_gray, dtype=np.float64)
    else: 
        convolved_image = np.zeros_like(src_gray, dtype=np.int16) 

    # 使用NumPy手动执行2D卷积
    for i in range(rows):
        for j in range(cols):
            # 从填充后的图像中提取感兴趣区域 (ROI)
            roi = padded_gray[i : i + k_rows, j : j + k_cols]
            
            # 应用卷积：逐元素相乘并求和
            convolution_sum = np.sum(roi * laplacian_kernel)
            convolved_image[i, j] = convolution_sum

    # 转换回uint8类型
    abs_dst = cv2.convertScaleAbs(convolved_image)
    
    return abs_dst