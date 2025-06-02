import pdb
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
import torch

from proj1_code.part3 import my_conv2d_pytorch
from proj1_code.utils import rgb2gray, load_image

ROOT = Path(__file__).resolve().parent.parent  # ../..


def test_my_conv2d_pytorch():
    """Assert that convolution output is correct, and groups are handled correctly
    for a 2-channel image with 4 filters (yielding 2 groups).
    """
    image = torch.zeros((1, 2, 3, 3), dtype=torch.float)
    image[0, 0] = torch.tensor(
        [
            [0, 1, 2],
            [3, 4, 5],
            [6, 7, 8]
        ]).float()
    image[0, 1] = torch.tensor(
        [
            [9, 10, 11],
            [12, 13, 14],
            [15, 16, 17]
        ]).float()

    identity_filter = torch.tensor([[0, 0, 0], [0, 1, 0], [0, 0, 0]]).float()
    double_filter = torch.tensor([[0, 0, 0], [0, 2, 0], [0, 0, 0]]).float()
    triple_filter = torch.tensor([[0, 0, 0], [0, 3, 0], [0, 0, 0]]).float()
    ones_filter = torch.ones(3, 3).float()
    filters = torch.stack(
        [identity_filter, double_filter, triple_filter, ones_filter], 0
    )

    filters = filters.reshape(4, 1, 3, 3).float()
    feature_maps = my_conv2d_pytorch(image.float(), filters)

    assert feature_maps.shape == torch.Size([1, 4, 3, 3])

    gt_feature_maps = torch.zeros((1, 4, 3, 3))

    # identity filter on channel 1
    gt_feature_maps[0, 0] = torch.tensor(
        [
            [0.0, 1.0, 2.0],
            [3.0, 4.0, 5.0],
            [6.0, 7.0, 8.0]
        ]
    )
    # doubling filter on channel 1
    gt_feature_maps[0, 1] = torch.tensor(
        [
            [0.0, 2.0, 4.0],
            [6.0, 8.0, 10.0],
            [12.0, 14.0, 16.0]
        ]
    )
    # tripling filter on channel 2
    gt_feature_maps[0, 2] = torch.tensor(
        [
            [27.0, 30.0, 33.0],
            [36.0, 39.0, 42.0],
            [45.0, 48.0, 51.0]
        ]
    )
    gt_feature_maps[0, 3] = torch.tensor(
        [
            [44.0, 69.0, 48.0],
            [75.0, 117.0, 81.0],
            [56.0, 87.0, 60.0]
        ]
    )

    assert torch.allclose(gt_feature_maps.float(), feature_maps.float() )