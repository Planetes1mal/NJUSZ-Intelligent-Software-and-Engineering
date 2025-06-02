#!/usr/bin/python3

import matplotlib.pyplot as plt
import numpy as np
import os
from pathlib import Path
import pdb

from proj1_code.utils import (
    vis_image_scales_numpy,
    im2single,
    single2im,
    load_image,
    save_image,
)

ROOT = Path(__file__).resolve().parent.parent  # ../..


def test_vis_image_scales_numpy():
    """Verify that the vis_hybrid_image function is working as anticipated."""
    fpath = f"{ROOT}/data/1a_dog.bmp"
    img = load_image(fpath)
    img_h, img_w, _ = img.shape

    img_scales = vis_image_scales_numpy(img)

    assert img_h == 361
    assert np.allclose(img_scales[:, :img_w, :], img)
    assert img_scales.shape == (361, 813, 3)
    assert isinstance(img_scales, np.ndarray)

    # #### For visualization only ####
    # plt.imshow( (img_scales * 255).astype(np.uint8) )
    # plt.show()
    # ################################


# def test_im2single_gray():
#     """ Convert 3-channel RGB image.


#     """
#     rgb_img = im2single(im_uint8)


def test_im2single_rgb():
    """Convert an image with values [0,255] to a single-precision floating
    point data type with values [0,1].

    """
    img = np.array(range(4 * 5 * 3), dtype=np.uint8)
    img = img.reshape(4, 5, 3)
    float_img = im2single(img)

    gt_float_img = np.array(range(4 * 5 * 3), dtype=np.uint8)
    gt_float_img = gt_float_img.reshape(4, 5, 3).astype(np.float32)
    gt_float_img /= 255.0
    assert np.allclose(gt_float_img, float_img)
    assert gt_float_img.dtype == float_img.dtype
    assert gt_float_img.shape == float_img.shape


def test_single2im():
    """
    Test conversion from single-precision floating point in [0,1] to
    uint8 in range [0,255].
    """
    float_img = np.array(range(4 * 5 * 3), dtype=np.uint8)
    float_img = float_img.reshape(4, 5, 3).astype(np.float32)
    float_img /= 255.0
    uint8_img = single2im(float_img)

    gt_uint8_img = np.array(range(4 * 5 * 3), dtype=np.uint8)
    gt_uint8_img = gt_uint8_img.reshape(4, 5, 3)

    assert np.allclose(gt_uint8_img, uint8_img)
    assert gt_uint8_img.dtype == uint8_img.dtype
    assert gt_uint8_img.shape == uint8_img.shape


def test_load_image():
    """ Load the dog image in `single` format."""
    fpath = f"{ROOT}/data/1a_dog.bmp"
    img = load_image(fpath)
    assert img.dtype == np.float32
    assert img.shape == (361, 410, 3)
    assert np.amin(img) >= 0.0
    assert np.amax(img) <= 1.0


def test_save_image():
    """ """
    save_fpath = "results/temp.png"

    # Create array as single-precision in [0,1]
    img = np.zeros((2, 3, 3), dtype=np.float32)
    img[0, 0, :] = 1
    img[1, 1, :] = 1
    save_image(save_fpath, img)
    assert Path(save_fpath).exists()
    os.remove(save_fpath)
