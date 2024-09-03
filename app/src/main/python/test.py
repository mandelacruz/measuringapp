def analyze_image(args):
    import numpy as np
    import cv2
    import os
    import io
    from PIL import Image

    image = args

    if image is None:
        return "Image not found!"
    else:
        return image