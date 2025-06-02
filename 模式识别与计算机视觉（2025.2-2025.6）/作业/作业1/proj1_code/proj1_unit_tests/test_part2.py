import pdb
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
import torch

from proj1_code.part2_datasets import HybridImageDataset
from proj1_code.part2_models import HybridImageModel
from proj1_code.utils import write_objects_to_file

ROOT = Path(__file__).resolve().parent.parent  # ../..


def test_dataloader_len() -> None:
    """Check dataloader __len__ for correct size (should be 5 pairs of images)."""
    img_dir = f"{ROOT}/data"
    cut_off_file = f"{ROOT}/cutoff_frequencies.txt"
    hid = HybridImageDataset(img_dir, cut_off_file)
    assert len(hid) == 5


def test_dataloader_get_item() -> None:
    """Verify that __getitem__ is implemented correctly, for the first dog/cat entry."""
    img_dir = f"{ROOT}/data"
    cut_off_file = f"{ROOT}/cutoff_frequencies.txt"
    hid = HybridImageDataset(img_dir, cut_off_file)

    first_item = hid[0]
    dog_img, cat_img, cutoff = first_item

    gt_size = [3, 361, 410]
    # low frequency should be 1a_dog.bmp, high freq should be cat
    assert [dog_img.shape[i] for i in range(3)] == gt_size
    assert [cat_img.shape[i] for i in range(3)] == gt_size

    # ground truth values
    dog_img_crop = torch.tensor(
        [
            [[0.4784, 0.4745], [0.5255, 0.5176]],
            [[0.4627, 0.4667], [0.5098, 0.5137]],
            [[0.4588, 0.4706], [0.5059, 0.5059]],
        ]
    )
    assert torch.allclose(dog_img[:, 100:102, 100:102], dog_img_crop, atol=1e-3)
    assert 0.0 < cutoff and cutoff < 1000.0


def test_pytorch_low_pass_filter_square_kernel() -> None:
    """Test the low pass filter, but not the output of the forward() pass."""
    hi_model = HybridImageModel()
    img_dir = f"{ROOT}/data"
    cut_off_file = f"{ROOT}/cutoff_frequencies.txt"

    # Dump to a file
    cutoff_freqs = [7, 7, 7, 7, 7]
    write_objects_to_file(fpath=cut_off_file, obj_list=cutoff_freqs)
    hi_dataset = HybridImageDataset(img_dir, cut_off_file)

    # should be the dog image
    img_a, img_b, cutoff_freq = hi_dataset[0]
    # turn CHW into NCHW
    img_a = img_a.unsqueeze(0)

    hi_model.n_channels = 3
    kernel = hi_model.get_kernel(cutoff_freq)
    pytorch_low_freq = hi_model.low_pass(img_a, kernel)

    assert list(pytorch_low_freq.shape) == [1, 3, 361, 410]
    assert isinstance(pytorch_low_freq, torch.Tensor)

    # crop from pytorch_output[:,:,20:22,20:22]
    gt_crop = torch.tensor(
        [
            [
                [[0.7941, 0.7989], [0.7906, 0.7953]],
                [[0.9031, 0.9064], [0.9021, 0.9052]],
                [[0.9152, 0.9173], [0.9168, 0.9187]],
            ]
        ],
        dtype=torch.float32,
    )
    assert torch.allclose(pytorch_low_freq[:, :, 20:22, 20:22], gt_crop, atol=1e-3)

    # ground truth element sum
    assert np.allclose(pytorch_low_freq.numpy().sum(), 209926.3481)


def test_low_freq_sq_kernel_pytorch() -> None:
    """Test the low frequencies that are an output of the forward pass."""
    dataset = HybridImageDataset(f"{ROOT}/data", f"{ROOT}/cutoff_frequencies.txt")
    dataloader = torch.utils.data.DataLoader(dataset)
    cutoff_freq = torch.Tensor([7])

    image_a, image_b, cutoff_freq = next(iter(dataloader))

    assert torch.allclose(
        cutoff_freq.float(), torch.Tensor([7])
    ), "Please pass a Pytorch tensor containing `7` as the cutoff frequency."
    assert isinstance(cutoff_freq, torch.Tensor), "Please pass a Pytorch tensor containing `7` as the cutoff frequency."

    model = HybridImageModel()

    low_frequencies, _, _ = model(image_a, image_b, cutoff_freq)

    img_a_val_sum = float(image_a.sum())
    assert np.allclose(img_a_val_sum, 215154.9531), "Dog image `1a_dog.bmp` should be the `image_a` argument."

    gt_low_freq_crop = torch.tensor(
        [
            [[0.5350, 0.5367], [0.5347, 0.5369]],
            [[0.5239, 0.5262], [0.5236, 0.5264]],
            [[0.5143, 0.5183], [0.5150, 0.5193]],
        ]
    )
    assert torch.allclose(
        gt_low_freq_crop, low_frequencies[0, :, 100:102, 100:102], atol=1e-3
    ), "Low freq vals incorrect"

    img_h = image_a.shape[2]
    img_w = image_a.shape[3]
    kernel = model.get_kernel(int(cutoff_freq))
    assert isinstance(kernel, torch.Tensor), "Kernel is not a torch tensor"

    gt_kernel_sz_list = [3, 1, 29, 29]
    kernel_sz_list = [int(val) for val in kernel.shape]

    assert gt_kernel_sz_list == kernel_sz_list, "Kernel is not the correct size"

    k_h = kernel.shape[2]
    k_w = kernel.shape[3]

    # Exclude the border pixels.
    low_freq_interior = low_frequencies[0, :, k_h : img_h - k_h, k_w : img_w - k_w]
    assert np.allclose(158332.06, float(low_freq_interior.sum()), atol=1), "Low frequency values are not correct"


def test_high_freq_sq_kernel_pytorch() -> None:
    """Test the high frequencies that are an output of the forward pass."""
    dataset = HybridImageDataset(f"{ROOT}/data", f"{ROOT}/cutoff_frequencies.txt")
    dataloader = torch.utils.data.DataLoader(dataset)
    cutoff_freq = torch.Tensor([7])

    image_a, image_b, cutoff_freq = next(iter(dataloader))

    model = HybridImageModel()

    _, high_frequencies, _ = model(image_a, image_b, cutoff_freq)

    assert isinstance(cutoff_freq, torch.Tensor), "Please pass a Pytorch tensor containing `7` as the cutoff frequency."
    assert torch.allclose(
        cutoff_freq.float(), torch.Tensor([7])
    ), "Please pass a Pytorch tensor containing `7` as the cutoff frequency."

    img_b_val_sum = float(image_b.sum())
    assert np.allclose(
        img_b_val_sum, 230960.1875, atol=5.0
    ), "Please pass in the cat image `1b_cat.bmp` as the `image_b` argument."

    gt_high_freq_crop = torch.tensor(
        [
            [[7.9527e-03, -7.6560e-03], [1.5484e-02, -6.9082e-05]],
            [[2.9861e-02, 2.2352e-02], [3.3504e-02, 3.3922e-02]],
            [[3.0958e-02, 2.7430e-02], [3.0706e-02, 3.1234e-02]],
        ]
    )
    assert torch.allclose(gt_high_freq_crop, high_frequencies[0, :, 100:102, 100:102], atol=1e-3)

    img_h = image_b.shape[2]
    img_w = image_b.shape[3]
    kernel = model.get_kernel(int(cutoff_freq))
    assert isinstance(kernel, torch.Tensor), "Kernel is not a torch tensor"

    gt_kernel_sz_list = [3, 1, 29, 29]
    kernel_sz_list = [int(val) for val in kernel.shape]

    assert gt_kernel_sz_list == kernel_sz_list, "Kernel is not the correct size"

    k_h = kernel.shape[2]
    k_w = kernel.shape[3]

    # Exclude the border pixels.
    high_freq_interior = high_frequencies[0, :, k_h : img_h - k_h, k_w : img_w - k_w]
    assert np.allclose(
        12.012651, float(high_freq_interior.sum()), atol=1e-1
    ), "Pytorch high frequencies values are not correct, please double check your implementation."


def test_hybrid_image_pytorch() -> None:
    """Compare output of the forward pass with known values."""
    dataset = HybridImageDataset(f"{ROOT}/data", f"{ROOT}/cutoff_frequencies.txt")
    dataloader = torch.utils.data.DataLoader(dataset)
    cutoff_freq = torch.Tensor([7])

    image_a, image_b, cutoff_freq = next(iter(dataloader))

    model = HybridImageModel()

    _, _, hybrid_image = model(image_a, image_b, cutoff_freq)

    _, _, img_h, img_w = image_b.shape
    kernel = model.get_kernel(int(cutoff_freq))
    _, _, k_h, k_w = kernel.shape

    # Exclude the border pixels.
    hybrid_interior = hybrid_image[0, :, k_h : img_h - k_h, k_w : img_w - k_w]
    assert np.allclose(
        158339.5469, hybrid_interior.sum(), atol=1e-2
    ), "Pytorch hybrid image values are not correct, please double check your implementation."

    # ground truth values
    gt_hybrid_crop = torch.tensor(
        [
            [[0.5430, 0.5291], [0.5502, 0.5368]],
            [[0.5537, 0.5486], [0.5571, 0.5604]],
            [[0.5452, 0.5457], [0.5457, 0.5506]],
        ]
    )
    # H,W,C order in Numpy
    assert torch.allclose(
        hybrid_image[0, :, 100:102, 100:102], gt_hybrid_crop, atol=1e-3
    ), "Pytorch hybrid image crop vals not correct"
