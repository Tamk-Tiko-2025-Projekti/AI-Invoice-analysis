import base64
import sys
import json
import os
from openai import OpenAI


test_run = False
if (sys.argv[2] == "true"):
    test_run = True

# Ensure an argument is passed
if len(sys.argv) < 2:
    print("Error: No image path provided.")
    sys.exit(1)

# Get the image path from command-line arguments
image_path = sys.argv[1]

# Function to encode the image
def encode_image(image_path):
    try:
        with open(image_path, "rb") as image_file:
            return base64.b64encode(image_file.read()).decode("utf-8")
    except FileNotFoundError:
        print(f"Error: File not found at {image_path}")
        sys.exit(1)
    except Exception as e:
        print(f"Error reading file: {e}")
        sys.exit(1)

base64_image = encode_image(image_path)

def make_api_request(image_path):
    # Encode the image
    base64_image = encode_image(image_path)
    client = OpenAI(api_key=os.environ["OPENAI_API_KEY"])

    # Prompt definition
    dev_prompt = """
    Extract invoice data into this JSON structure:
    {
        "content": {
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
        },
        "error": {
        }
    }

    Rules:
    1. Include ONLY fields present in the image
    2. Dates: YYYY-MM-DD
    3. Numbers: 2 decimals
    4. If ANY req fields are missing, list them in "error"
    5. Bank: reference OR message must exist (add to "error" if both missing)
    6. Return NO other text or explanations
    7. Return the JSON only, do not add any backticks or other formatting
    """
    # OpenAI API request
    completion = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {
                "role": "developer",
                "content": [{"type": "text", "text": dev_prompt}],
            },
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": "Fetch me text from my invoice in JSON format"},
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": f"data:image/jpeg;base64,{base64_image}",
                        },
                    },
                ],
            }
        ],
    )

    return completion.choices[0].message.content


# result = completion.choices[0].message.content
if (test_run == False):
    result = make_api_request(image_path)
else:
    result = """
    {
        "content": {
            "purchase_invoice_type_id": 1,
            "supplier_name": "Test Company Oy",
            "supplier_vat_number": "FI12345678",
            "supplier_business_id": "1234567-8",
            "supplier_country": "FI",
            "invoice_number": "INV-2024-001",
            "invoice_date": "2024-03-20",
            "due_date": "2024-04-19",
            "bank_account": "FI21 1234 5600 0007 85",
            "bank_bic": "NDEAFIHH",
            "bank_reference": "RF18 1234 5678 9012 34",
            "total_gross": 1234.56,
            "currency": "EUR"
        },
        "error": {}
    }
    """

try:
    json_result = json.loads(result)
    print(json.dumps(json_result, indent=4, ensure_ascii=False))
except json.JSONDecodeError:
    print("Error: Failed to decode JSON response.")
    print(f"Response: {result}")
except Exception as e:
    print(f"Error: {e}")
