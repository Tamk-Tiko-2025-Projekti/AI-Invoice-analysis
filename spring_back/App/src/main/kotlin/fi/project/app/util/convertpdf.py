from pdf2image import convert_from_path

image = convert_from_path('./temp/test.pdf', dpi=90, fmt="webp")

for i in range(len(image)):
    image[i].save('./temp/test' + str(i) + '.webp', 'webp')
