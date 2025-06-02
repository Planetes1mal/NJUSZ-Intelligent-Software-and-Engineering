import cv2
import numpy as np

def StandSobel(src, ddepth=cv2.CV_16S, scale=1, delta=0, borderType=cv2.BORDER_DEFAULT):
    src = cv2.GaussianBlur(src, (3, 3), 0)
    
    gray = cv2.cvtColor(src, cv2.COLOR_BGR2GRAY)
    
    grad_x = cv2.Sobel(gray, ddepth, 1, 0, ksize=3, scale=scale, delta=delta, borderType=cv2.BORDER_DEFAULT)
    grad_y = cv2.Sobel(gray, ddepth, 0, 1, ksize=3, scale=scale, delta=delta, borderType=cv2.BORDER_DEFAULT)
    
    abs_grad_x = cv2.convertScaleAbs(grad_x)
    abs_grad_y = cv2.convertScaleAbs(grad_y)

    sobelCombined = cv2.bitwise_or(abs_grad_x, abs_grad_y)

    return sobelCombined

def StandSobel(src, ddepth=cv2.CV_16S, scale=1, delta=0, borderType=cv2.BORDER_DEFAULT):
    """
    Applies the standard OpenCV Sobel operator.
    Args:
        src: Input image.
        ddepth: Desired depth of the output image.
        scale: Optional scale factor for the computed derivative values.
        delta: Optional delta value that is added to the results.
        borderType: Pixel extrapolation method.
    Returns:
        Combined Sobel edge image.
    """
    # 使用高斯滤波去除噪声
    src = cv2.GaussianBlur(src, (3, 3), 0)
    
    # 将图像转换为灰度图
    gray = cv2.cvtColor(src, cv2.COLOR_BGR2GRAY)
    
    # 计算x和y方向的梯度
    # cv2.Sobel(src, ddepth, dx, dy, ksize, scale, delta, borderType)
    grad_x = cv2.Sobel(gray, ddepth, 1, 0, ksize=3, scale=scale, delta=delta, borderType=borderType)
    grad_y = cv2.Sobel(gray, ddepth, 0, 1, ksize=3, scale=scale, delta=delta, borderType=borderType)
    
    # 转换为绝对值并转回uint8类型
    abs_grad_x = cv2.convertScaleAbs(grad_x)
    abs_grad_y = cv2.convertScaleAbs(grad_y)

    # 组合梯度 (OpenCV example often uses addWeighted or bitwise_or)
    # The provided template used bitwise_or
    sobelCombined = cv2.bitwise_or(abs_grad_x, abs_grad_y)
    # If you wanted to match the mathematical G = sqrt(Gx^2 + Gy^2):
    # grad_x_abs = np.abs(grad_x) # or use grad_x directly if ddepth is float
    # grad_y_abs = np.abs(grad_y)
    # sobel_mag = np.sqrt(grad_x_abs**2 + grad_y_abs**2)
    # sobelCombined = cv2.convertScaleAbs(sobel_mag) # then convert to uint8

    return sobelCombined

def CustomSobel(src, ddepth=cv2.CV_16S, scale=1, delta=0, borderType=cv2.BORDER_DEFAULT):
    # 使用高斯滤波去除噪声
    src_blurred = cv2.GaussianBlur(src, (3, 3), 0)
    
    # 将图像转换为灰度图
    gray = cv2.cvtColor(src_blurred, cv2.COLOR_BGR2GRAY)
    
    # 将灰度图像转换为float32进行计算
    gray_float = gray.astype(np.float32)
    
    # 定义索贝尔算子卷积核
    # 定义Sobel X方向的卷积核
    sobel_x_kernel = np.array([
        [-1, 0, 1],
        [-2, 0, 2],
        [-1, 0, 1]
    ], dtype=np.float32)
    
    # 定义Sobel Y方向的卷积核
    sobel_y_kernel = np.array([
        [ 1,  2,  1],
        [ 0,  0,  0],
        [-1, -2, -1]
    ], dtype=np.float32)
    
    # 将OpenCV的borderType映射到NumPy的padding mode
    np_border_mode = _map_cv2_border_to_numpy(borderType)
    
    # 应用水平和垂直方向的卷积核 (纯卷积)
    # 使用自定义的NumPy卷积函数计算x方向梯度（原始卷积结果）
    grad_x_conv = _numpy_convolve2d(gray_float, sobel_x_kernel, np_border_mode)
    # 使用自定义的NumPy卷积函数计算y方向梯度（原始卷积结果）
    grad_y_conv = _numpy_convolve2d(gray_float, sobel_y_kernel, np_border_mode)
    
    # 应用缩放因子和平移量
    grad_x_processed = grad_x_conv * scale + delta
    grad_y_processed = grad_y_conv * scale + delta
    
    # (例如, cv2.CV_16S -> np.int16, cv2.CV_32F -> np.float32)
    grad_x_ddepth = _convert_to_ddepth(grad_x_processed, ddepth)
    grad_y_ddepth = _convert_to_ddepth(grad_y_processed, ddepth)
    
    # 计算梯度绝对值并转换为uint8类型
    abs_grad_x_uint8 = np.clip(np.abs(grad_x_ddepth), 0, 255).astype(np.uint8)
    abs_grad_y_uint8 = np.clip(np.abs(grad_y_ddepth), 0, 255).astype(np.uint8)

    # 组合水平和垂直梯度（使用加权平均）
    abs_grad_x_f = abs_grad_x_uint8.astype(np.float32)
    abs_grad_y_f = abs_grad_y_uint8.astype(np.float32)
    
    sobel_combined_float = 0.5 * abs_grad_x_f + 0.5 * abs_grad_y_f
    sobelCombined = np.clip(sobel_combined_float, 0, 255).astype(np.uint8)
    
    return sobelCombined

def _map_cv2_border_to_numpy(cv2_border_type):
    if cv2_border_type == cv2.BORDER_CONSTANT:
        return 'constant'
    elif cv2_border_type == cv2.BORDER_REPLICATE:
        return 'edge'
    elif cv2_border_type == cv2.BORDER_REFLECT:
        return 'symmetric'
    elif cv2_border_type == cv2.BORDER_WRAP:
        return 'wrap'
    elif cv2_border_type == cv2.BORDER_REFLECT_101 or cv2_border_type == cv2.BORDER_DEFAULT:
        return 'reflect'
    else:
        print(f"Warning: Unsupported cv2_border_type {cv2_border_type}. Defaulting to 'reflect'.")
        return 'reflect'

def _numpy_convolve2d(image_float, kernel, border_mode_numpy, border_constant_values=0):
    rows, cols = image_float.shape
    k_rows, k_cols = kernel.shape
    
    pad_rows = k_rows // 2
    pad_cols = k_cols // 2
    
    if border_mode_numpy == 'constant':
        padded_image = np.pad(image_float, 
                              ((pad_rows, pad_rows), (pad_cols, pad_cols)), 
                              mode=border_mode_numpy, 
                              constant_values=border_constant_values)
    else:
        padded_image = np.pad(image_float, 
                              ((pad_rows, pad_rows), (pad_cols, pad_cols)), 
                              mode=border_mode_numpy)
    
    convolved_image = np.zeros_like(image_float, dtype=np.float32)
    
    for i in range(rows):
        for j in range(cols):
            roi = padded_image[i : i + k_rows, j : j + k_cols]
            convolution_sum = np.sum(roi * kernel)
            convolved_image[i, j] = convolution_sum
            
    return convolved_image

def _convert_to_ddepth(image_float, ddepth_cv):
    if ddepth_cv == cv2.CV_16S:
        return np.round(image_float).astype(np.int16)
    elif ddepth_cv == cv2.CV_32F:
        return image_float.astype(np.float32)
    elif ddepth_cv == cv2.CV_64F:
        return image_float.astype(np.float64)
    elif ddepth_cv == cv2.CV_8U:
        return np.round(np.clip(image_float, 0, 255)).astype(np.uint8)
    else:
        raise ValueError(f"Unsupported ddepth_cv value: {ddepth_cv}")