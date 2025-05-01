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
    print(f"Converting {input_pdf} to WEBP...")
    images = convert_from_path(input_pdf, dpi=90, fmt="webp")
    images[0].save(output_path, 'WEBP')
    print(f"Successfully saved to {output_path}")
    os.remove(input_pdf) # Remove the original PDF file
    
except Exception as e:
    print(f"Conversion failed: {str(e)}")
    sys.exit(1)
