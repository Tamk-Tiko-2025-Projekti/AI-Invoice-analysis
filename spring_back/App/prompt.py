import base64
import sys
import json
import os
# import signal
from openai import OpenAI
from dotenv import load_dotenv

# Parse command-line arguments
image_path = sys.argv[1]
dev_prompt_path = sys.argv[2]
user_prompt_path = sys.argv[3]
test_run = sys.argv[4].lower() == "true"
expect_json = sys.argv[5].lower() == "true"


# TIMEOUT_SECONDS = 60

# def timeout_handler(signum, frame):
#     raise TimeoutError(f"API request timed out (>{TIMEOUT_SECONDS} seconds)")

# Function to encode the image
def encode_image(path):
    try:
        with open(path, "rb") as image_file:
            return base64.b64encode(image_file.read()).decode("utf-8")
    except FileNotFoundError:
        print(f"Error: File not found at {path}")
        sys.exit(1)
    except Exception as e:
        print(f"Error reading file: {e}")
        sys.exit(1)


# If not test run, read developer and user prompts from file
if not test_run:
    try:
        with open(dev_prompt_path, "r", encoding="utf-8") as file:
            dev_prompt = file.read()
    except Exception as e:
        print(f"Error reading developer prompt file: {e}")
        sys.exit(1)

    try:
        with open(user_prompt_path, "r", encoding="utf-8") as file:
            user_prompt = file.read()
    except Exception as e:
        print(f"Error reading user prompt file: {e}")
        sys.exit(1)

# Optional image encoding
base64_image = None
if image_path != "-":
    base64_image = encode_image(image_path)


# Function to send API request
def make_api_request():
    # This code only works on UNIX systems
    # signal.signal(signal.SIGALRM, timeout_handler)
    # signal.alarm(TIMEOUT_SECONDS)  # Set timeout
    try:
        # Load environment variables
        load_dotenv()
        client = OpenAI(api_key=os.environ["OPENAI_API_KEY"])

        user_content = [{"type": "text", "text": user_prompt}]
        if base64_image:
            user_content.append({
                "type": "image_url",
                "image_url": {
                    "url": f"data:image/jpeg;base64,{base64_image}",
                },
            })

        completion = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {
                    "role": "developer",
                    "content": [{"type": "text", "text": dev_prompt}],
                },
                {
                    "role": "user",
                    "content": user_content,
                }
            ],
        )

        # signal.alarm(0)  # Cancel timeout
        return completion.choices[0].message.content
    except TimeoutError:
        print("Error: API request timed out.")
        sys.exit(1)


# Use test response or make real API call
if test_run:
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
else:
    result = make_api_request()

# Output result
if expect_json:
    try:
        json_result = json.loads(result)
        print(json.dumps(json_result, indent=4, ensure_ascii=False))
    except json.JSONDecodeError:
        print("Error: Failed to decode JSON response.")
        print(f"Response: {result}")
    except Exception as e:
        print(f"Error: {e}")
else:
    print(result)
