import sys
import os
from pdf2image import convert_from_path

if len(sys.argv) < 2:
    print("Usage: python convertpdf.py <input_pdf> [output_dir]")
    sys.exit(1)

input_pdf = sys.argv[1]
output_dir = sys.argv[2] if len(sys.argv) > 2 else os.path.dirname(input_pdf)
output_path = os.path.join(output_dir, "temp.webp")

try:
    images = convert_from_path(input_pdf, dpi=90, fmt="webp")
    images[0].save(output_path, 'WEBP')
    try:
        os.remove(input_pdf) # Remove the original PDF file
        print(f"Original PDF {input_pdf} deleted successfully.")
    except OSError as delete_error:
        print(f"Failed to delete the original PDF {input_pdf}. Error: {str(delete_error)}")

except Exception as e:
    print(e)
    sys.exit(1)
