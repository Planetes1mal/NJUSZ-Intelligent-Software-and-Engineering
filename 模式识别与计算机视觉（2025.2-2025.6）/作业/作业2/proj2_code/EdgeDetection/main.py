import cv2
import os
import argparse
from canny import CustomCanny, StandCanny
from sobel import CustomSobel, StandSobel
from laplacian import CustomLaplacian, StandLaplacian
import numpy as np

# def cal_metric(preds_vis, gt_vis):
#     """
#     Calculate accuracy, recall, and F1 score based on visible points.
#     Args:
#         @preds_vis: Predicted visibility flags
#         @gt_vis: Ground truth visibility flags

#     Returns:
#         Tuple of (accuracy, precision, recall, F1 score)
#     """
#     # Ensure inputs are numpy arrays
#     preds_vis = np.array(preds_vis)
#     gt_vis = np.array(gt_vis)

#     assert set(preds_vis) == {0, 255}, "Predictions should be binary (0 or 255)"
#     assert set(gt_vis) == {0, 255}, "Ground truth should be binary (0 or 255)"

#     # Calculate true positives, true negatives, false positives, and false negatives
#     tp = np.sum((preds_vis == 255) & (gt_vis == 255))
#     tn = np.sum((preds_vis == 0) & (gt_vis == 0))
#     fp = np.sum((preds_vis == 255) & (gt_vis == 0))
#     fn = np.sum((preds_vis == 0) & (gt_vis == 255))

#     # Calculate accuracy, precision, recall, and F1 score
#     accuracy = (tp + tn) / (tp + tn + fp + fn) if (tp + tn + fp + fn) > 0 else 0
#     precision = tp / (tp + fp) if (tp + fp) > 0 else 0
#     recall = tp / (tp + fn) if (tp + fn) > 0 else 0
#     f1_score = (2 * tp) / (2 * tp + fp + fn) if (2 * tp + fp + fn) > 0 else 0

#     return accuracy, precision, recall, f1_score


if __name__ == "__main__":
    
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", type=str, default="sobel", choices=["canny", "sobel", "laplacian"])
    parser.add_argument("--input_dir", type=str, default="data/")
    parser.add_argument("--output_dir", type=str, default="output/sobel")
    parser.add_argument("--lowThreshold", type=int, default=50)
    parser.add_argument("--highThreshold", type=int, default=150)
    args = parser.parse_args()

    if not os.path.exists(args.output_dir):
        os.makedirs(args.output_dir)

    for filename in sorted(os.listdir(args.input_dir)):
        img = cv2.imread(os.path.join(args.input_dir, filename))

        if args.mode == "laplacian":
            stand_edge = StandLaplacian(img)
            custom_edge = CustomLaplacian(img)

        elif args.mode == "sobel":
            stand_edge = StandSobel(img)
            custom_edge = CustomSobel(img)

        elif args.mode == "canny":
            stand_edge = StandCanny(img, args.lowThreshold, args.highThreshold)
            custom_edge = CustomCanny(img, args.lowThreshold, args.highThreshold)

        else:
            raise ValueError("Invalid mode. Choose 'canny', 'sobel' or 'laplacian'.")

        cv2.imwrite(os.path.join(args.output_dir, filename), cv2.hconcat((stand_edge, custom_edge)))

        # # Calculate accuracy
        # accuracy, precision, recall, f1_score = cal_metric(custom_edge.flatten(), stand_edge.flatten())
        # print(f"Image: {filename}, Accuracy: {accuracy:.2f}, Precision: {precision:.2f}, Recall: {recall:.2f}, F1 Score: {f1_score:.2f}")
        # exit()

        # with open(os.path.join(args.output_dir, "accuracy.txt"), "a") as f:
        #     f.write(f"{filename}: {accuracy:.2f}\n")