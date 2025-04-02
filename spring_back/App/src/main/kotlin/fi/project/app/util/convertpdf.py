import sys

from pdf2image import convert_from_path

for i in range(1, len(sys.argv)):
    print(sys.argv[i])

if (len(sys.argv) < 2):
    print("Please provide a PDF file path.")
    sys.exit(1)

image = convert_from_path('./temp/temp.pdf', dpi=90, fmt="webp")
image[0].save('./temp/temp' + '.webp', 'webp')
