import base64
from openai import OpenAI

client = OpenAI(api_key="")
# Function to encode the image
def encode_image(image_path):
    with open(image_path, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode("utf-8")

image_path = ""

base64_image = encode_image(image_path)

dev_prompt = ""

user_prompt = ""

completion = client.chat.completions.create(
    model="gpt-4o",
    messages=[
        {
            "role": "developer",
            "content": [
                {
                    "type": "text",
                    "text": """
                    Return EXACTLY this JSON structure with NO additional text.
                    If any req fields are missing, append an error property with the missing fields at end of the JSON.
                    {
                    "purchase_invoice_type_id": (1=invoice, 2=credit, req),
                    "purchase_supplier_id": (str),
                    "supplier_name": (str, req),
                    "supplier_vat_number": (str),
                    "supplier_business_id": (str),
                    "supplier_country": (str, default=FI),
                    "invoice_number": (str),
                    "credited_invoice_no": (str),
                    "invoice_date": (date, req),
                    "due_date": (date, req),
                    "discount_date1": (date),
                    "discount_percent1": (num),
                    "discount_sum1": (num),
                    "entry_date": (date),
                    "bank_account": (str, req),
                    "bank_bic": (str, req),
                    "bank_reference": (str),
                    "bank_message": (str),
                    "total_net": (num),
                    "total_gross": (num, req),
                    "currency": (str, default=EUR),
                    "order_number": (str),
                    "your_reference": (str),
                    "our_reference": (str),
                    "terms_of_payment": (str),
                    "payable_account_code": (num)
                    }
                    Bank reference OR bank message must be given.
                    """
                },
            ],
        },

        {
            "role": "user",
            "content": [
                { "type": "text", "text": "Give me JSON text from this image" },
                {
                    "type": "image_url",
                    "image_url": {
                        "url": f"data:image/jpeg;base64,{base64_image}",
                        "detail": "low",
                    },
                },
            ],
        }
    ],
)

print(completion.choices[0].message.content)