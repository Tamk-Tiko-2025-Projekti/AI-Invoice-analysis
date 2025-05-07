from pyzbar.pyzbar import decode # https://pypi.org/project/pyzbar/
from PIL import Image
import sys
import json

if len(sys.argv) < 2:
    print("Usage: python readBarCode.py <image_path>")
    sys.exit(1)

image_path = sys.argv[1]

image = Image.open(image_path)
image = image.convert('L') # Convert to 8-bit grayscale

decoded_objects = decode(image)
# Check if the image was decoded successfully
if not decoded_objects:
    print("Failed to decode the image.")
    sys.exit(1)
barcode = None
# Check if the barcode is in CODE128 format
for obj in decoded_objects:
    print(f"Found barcode: Type={obj.type}, Data={obj.data}")
    if obj.type == "CODE128":
        barcode = obj.data.decode("utf-8")
        break
if not barcode:
    print("No CODE128 barcode found in the image.")
    sys.exit(1)


# Check if the barcode is in Finnish virtual barcode format
if len(barcode) < 54 or not barcode.startswith("4"):
    raise ValueError("Invalid barcode format")

# Extract the relevant parts of the barcode
iban = barcode[1:17].lstrip('0')
euros = barcode[17:23].lstrip('0')
cents = barcode[23:25]
reference = barcode[25:48].lstrip('0')
due_date = barcode[48:54]
# Format the date to DD/MM/YYYYY from YYMMDD
due_date = f"{due_date[4:6]}/{due_date[2:4]}/20{due_date[:2]}"

content = {
    "bank_account": f"FI{iban}",
    "total_gross": f"{euros}.{cents}",
    "bank_reference": reference,
    "due_date": due_date
}
# Print the content as JSON
print(json.dumps(content, indent=4))
