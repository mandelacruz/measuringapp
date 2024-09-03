import numpy as np
import cv2
import os
import io
from PIL import Image
import json


def return_image(args):
    image_path = args

    directory_path = os.path.dirname(image_path)
    filename = os.path.basename(image_path)

    image = cv2.imread(image_path)

    if image is None:
        return "Image not found!"
    else:
        h = 650
        w = 650

        resized_image = cv2.resize(image, (w, h))

        hsv = cv2.cvtColor(resized_image, cv2.COLOR_BGR2HSV)

        lower_white = np.array([0, 0, 165])
        higher_white = np.array([255, 255, 255])

        white_range = cv2.inRange(hsv, lower_white, higher_white)

        # Apply thresholding to binarize the image (adjust the threshold value as needed)
        _, thresh = cv2.threshold(white_range, 100, 255, cv2.THRESH_BINARY)

        # Find contours in the binary image
        contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        counter = 1

        # Look for the outer bounding boxes (no children):
        for _, c in enumerate(contours):
            # Get blob area:
            currentArea = cv2.contourArea(c)
            # Set a min area threshold:
            minArea = 100

            if currentArea > minArea:
                # Approximate the contour to a polygon:
                contoursPoly = cv2.approxPolyDP(c, 3, True)

                # Get the polygon's bounding rectangle:
                boundRect = cv2.boundingRect(contoursPoly)

                # Get the dimensions of the bounding rect:
                rectX = boundRect[0]
                rectY = boundRect[1]
                rectWidth = boundRect[2]
                rectLength = boundRect[3]

                # Calculate rectangle length (diagonal)
                #rectLength = np.sqrt(rectWidth**2 + rectLength**2)

                # Draw bounding box around the rice grain
                x, y, w, h = cv2.boundingRect(c)
                cv2.rectangle(resized_image, (x, y), (x + w, y + h), (0, 255, 0), 1)

                # Draw Rotated Rectangle (paslash lang)
                rect = cv2.minAreaRect(c)
                box = cv2.boxPoints(rect)
                box = np.int64(box)
                cv2.drawContours(resized_image,[box],0,(255, 255, 0),1)

                # Value W and H rotated
                (x, y), (width, length), angle = rect
                aspect_ratio = min(width, length) / max(width, length)

                label = f"G{counter}"

                # Add text - Syntax: cv2.putText(image, text, org, font, fontScale, color, thickness, lineType, bottomLeftOrigin(optional: True of False))
                cv2.putText(resized_image, label, (int(x) - 12, int(y) + 7), cv2.FONT_HERSHEY_SIMPLEX, .6, (0, 10, 255), 1, cv2.LINE_AA)

                counter = counter + 1

        # Save image with plots
        image_arr = Image.fromarray(resized_image)

        output_path = directory_path + "/copy-" + filename
        image_arr.save(output_path)

        return output_path

def analyze_image(args):
    image_path = args

    directory_path = os.path.dirname(image_path)

    image = cv2.imread(image_path)

    if image is None:
        return "Image not found!"
    else:
        h = 650
        w = 650

        resized_image = cv2.resize(image, (w, h))

        hsv = cv2.cvtColor(resized_image, cv2.COLOR_BGR2HSV)

        lower_white = np.array([0, 0, 165])
        higher_white = np.array([255, 255, 255])

        white_range = cv2.inRange(hsv, lower_white, higher_white)

        # Apply thresholding to binarize the image (adjust the threshold value as needed)
        _, thresh = cv2.threshold(white_range, 100, 255, cv2.THRESH_BINARY)

        # Find contours in the binary image
        contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        # Store the bounding rectangles here:
        rectanglesList = []

        data = []

        counter = 1
        length_all = 0

        # Look for the outer bounding boxes (no children):
        for _, c in enumerate(contours):
            # Get blob area:
            currentArea = cv2.contourArea(c)
            # Set a min area threshold:
            minArea = 100

            if currentArea > minArea:
                # Approximate the contour to a polygon:
                contoursPoly = cv2.approxPolyDP(c, 3, True)

                # Get the polygon's bounding rectangle:
                boundRect = cv2.boundingRect(contoursPoly)

                # get dimensions of image
                img_dimensions = contoursPoly.shape

                # Store rectangles in list:
                rectanglesList.append(boundRect)

                # Get the dimensions of the bounding rect:
                rectX = boundRect[0]
                rectY = boundRect[1]
                rectWidth = boundRect[2]
                rectLength = boundRect[3]

                # Calculate rectangle length (diagonal)
                #rectLength = np.sqrt(rectWidth**2 + rectLength**2)

                # Convert pixels to centimeter
                #rectWidthCm = rectWidth * 0.010040
                #ectLengthCm = rectLength * 0.010284
                #rectLengthCm = rectLength * 0.010300

                # Convert pixels to Millimeter
                #rectWidthMm = rectWidth * 0.10040
                #rectLengthMm = rectLength * 0.10284
                #rectLengthMm = rectLength * 0.10300

                # Calculate dimension
                #dimension = rectWidth * rectLength * rectLength
                #dimensionCm = dimension * 0.010315

                # Calculate area of the contour
                #area = cv2.contourArea(c)

                # Calculate equivalent diameter
                #eq_diameter = np.sqrt(4 * area / np.pi)

                # Draw bounding box around the rice grain
                x, y, w, h = cv2.boundingRect(c)
                cv2.rectangle(resized_image, (x, y), (x + w, y + h), (0, 255, 0), 1)

                # Draw Rotated Rectangle (paslash lang)
                rect = cv2.minAreaRect(c)
                box = cv2.boxPoints(rect)
                box = np.int64(box)
                cv2.drawContours(resized_image,[box],0,(0,0,255),1)


                # Value W and H rotated
                (x, y), (width, length), angle = rect
                aspect_ratio = min(width, length) / max(width, length)

                # Add text - Syntax: cv2.putText(image, text, org, font, fontScale, color, thickness, lineType, bottomLeftOrigin(optional: True of False))
                #cv2.putText(resized_image, str(counter), (int(x), int(y)), cv2.FONT_HERSHEY_SIMPLEX, .5, (255, 0, 0), 2, cv2.LINE_AA)


                # Swap the values using tuple unpacking when width is greater than length
                if width > length:
                    width, length = length, width

                # Convert pixels to centimeter
                rotLengthCm = length * 0.010284
                rotWidthCm = width * 0.010040

                # # Convert pixels to Millimeter for Android Phone
                #rotLengthMm = length * 0.10284
                #rotWidthMm = width * 0.10040

                # Convert pixels to Millimeter for Android Tablet
                rotLengthMm = length * 0.10410
                rotWidthMm = width * 0.10157

                counter = counter + 1

                length_all = length_all + rotLengthMm

                new_data = {
                    "n" : counter - 1,
                    "length" : round(rotLengthMm, 2),
                    "width" : round(rotWidthMm, 2),
                }

                data.append(new_data)

        data_json = json.dumps(data)

        # Remove this to save original photo in Android/data/com.example.python/files/Pictures
        os.remove(image_path)

        return data_json

