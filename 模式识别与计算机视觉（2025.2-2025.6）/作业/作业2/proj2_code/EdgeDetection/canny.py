import cv2
import numpy as np

def StandCanny(img, lowThreshold, highThreshold):
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray, (5, 5), 1.4)
    edges = cv2.Canny(blurred, lowThreshold, highThreshold)
    return edges

def CustomCanny(img, lowThreshold, highThreshold):
    # 步骤1：使用高斯滤波去除噪声
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    blurred_img = cv2.GaussianBlur(gray, (5, 5), 1.0)
    
    # 步骤2：使用Sobel算子计算梯度
    blurred_img_float64 = blurred_img.astype(np.float64)

    # 使用自定义的Sobel算子计算梯度
    Kx = np.array([[-1, 0, 1],
                   [-2, 0, 2],
                   [-1, 0, 1]], dtype=np.float64)
    Ky = np.array([[1, 2, 1],
                   [0, 0, 0],
                   [-1, -2, -1]], dtype=np.float64)

    # 对 Sobel 算子使用自定义的 NumPy 卷积
    border_mode_for_sobel = _map_cv2_border_to_numpy_canny(cv2.BORDER_REPLICATE)

    # 计算x方向梯度
    Ix = _numpy_convolve2d_float64(blurred_img_float64, Kx, border_mode_for_sobel)
    # 计算y方向梯度
    Iy = _numpy_convolve2d_float64(blurred_img_float64, Ky, border_mode_for_sobel)

    # 计算梯度幅值和方向
    gradient_magnitude = np.sqrt(Ix**2 + Iy**2)
    gradient_angle = np.arctan2(Iy, Ix) * 180 / np.pi
    gradient_angle[gradient_angle < 0] += 180

    # 步骤3：非极大值抑制 (NMS)
    M, N = gradient_magnitude.shape
    nms_output = np.zeros((M, N), dtype=np.float64)

    for i in range(1, M - 1):
        for j in range(1, N - 1):
            mag_curr = gradient_magnitude[i, j]
            angle_curr = gradient_angle[i, j]

            # 根据梯度方向确定邻居像素
            # 角度 0/180: 水平边缘 (梯度垂直)
            if (0 <= angle_curr < 22.5) or (157.5 <= angle_curr <= 180):
                neighbor1_mag = gradient_magnitude[i, j + 1]
                neighbor2_mag = gradient_magnitude[i, j - 1]
            # 角度 45: 对角线边缘
            elif (22.5 <= angle_curr < 67.5):
                neighbor1_mag = gradient_magnitude[i - 1, j + 1]
                neighbor2_mag = gradient_magnitude[i + 1, j - 1]
            # 角度 90: 垂直边缘 (梯度水平)
            elif (67.5 <= angle_curr < 112.5):
                neighbor1_mag = gradient_magnitude[i - 1, j]
                neighbor2_mag = gradient_magnitude[i + 1, j]
            # 角度 135: 对角线边缘
            elif (112.5 <= angle_curr < 157.5):
                neighbor1_mag = gradient_magnitude[i - 1, j - 1]
                neighbor2_mag = gradient_magnitude[i + 1, j + 1]
            else:
                neighbor1_mag = mag_curr 
                neighbor2_mag = mag_curr
            
            # 如果不是局部最大值则抑制
            if mag_curr >= neighbor1_mag and mag_curr >= neighbor2_mag:
                nms_output[i, j] = mag_curr
            else:
                nms_output[i, j] = 0
    
    # 步骤4：双阈值处理 和 步骤5：使用滞后阈值处理进行边缘跟踪
    final_edges = np.zeros((M, N), dtype=np.uint8)

    strong_edge_val = 255 # 强边缘的值

    # 找到所有强边缘
    strong_r, strong_c = np.where(nms_output >= highThreshold)

    final_edges[strong_r, strong_c] = strong_edge_val

    # 使用栈来存储强边缘像素
    pixel_stack = []
    for r, c in zip(strong_r, strong_c):
        if 1 <= r < M - 1 and 1 <= c < N - 1:
             pixel_stack.append((r, c))

    while len(pixel_stack) > 0:
        curr_r, curr_c = pixel_stack.pop()

        for dr in [-1, 0, 1]:
            for dc in [-1, 0, 1]:
                if dr == 0 and dc == 0:
                    continue

                nr, nc = curr_r + dr, curr_c + dc

                # 检查邻居是否在边界内
                if 0 <= nr < M and 0 <= nc < N:
                    is_weak_candidate = (nms_output[nr, nc] >= lowThreshold) and \
                                      (nms_output[nr, nc] < highThreshold)
                    
                    if is_weak_candidate and final_edges[nr, nc] == 0:
                        final_edges[nr, nc] = strong_edge_val # 将弱边缘提升为强边缘
                        pixel_stack.append((nr, nc))
    return final_edges

def _map_cv2_border_to_numpy_canny(cv2_border_type):
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
        print(f"Warning: Unsupported cv2_border_type {cv2_border_type} in Canny. Defaulting to 'edge'.")
        return 'edge'

def _numpy_convolve2d_float64(image_float64, kernel_float64, border_mode_numpy, border_constant_values=0):
    rows, cols = image_float64.shape
    k_rows, k_cols = kernel_float64.shape
    
    pad_rows = k_rows // 2
    pad_cols = k_cols // 2
    
    if border_mode_numpy == 'constant':
        padded_image = np.pad(image_float64, 
                              ((pad_rows, pad_rows), (pad_cols, pad_cols)), 
                              mode=border_mode_numpy, 
                              constant_values=border_constant_values)
    else:
        padded_image = np.pad(image_float64, 
                              ((pad_rows, pad_rows), (pad_cols, pad_cols)), 
                              mode=border_mode_numpy)
    
    convolved_image = np.zeros_like(image_float64, dtype=np.float64)
    
    for i in range(rows):
        for j in range(cols):
            roi = padded_image[i : i + k_rows, j : j + k_cols]
            convolution_sum = np.sum(roi * kernel_float64)
            convolved_image[i, j] = convolution_sum
            
    return convolved_image