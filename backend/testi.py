import requests
import json
import base64
import sys

# Ollama API endpoint
OLLAMA_URL = "http://localhost:11434/api/generate"

def encode_image(image_path):
    try:
        with open(image_path, "rb") as image_file:
            return base64.b64encode(image_file.read()).decode("utf-8")
    except FileNotFoundError:
        print(json.dumps({"error": f"File not found: {image_path}"}))
        sys.exit(1)
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

def query_ollama_with_image(image_path, prompt="", model="gemma3:12b"):
    base64_image = encode_image(image_path)

    payload = {
        "model": model,
        "prompt": prompt,
        "images": [base64_image],  # Ollama expects images as base64 strings inside a list
        "stream": False  # Set to True for streaming but requires changes in other parts of code too
    }

    try:
        response = requests.post(OLLAMA_URL, json=payload)
        response.raise_for_status()
        print(json.dumps(response.json()))
        sys.exit(0)
    except requests.exceptions.RequestException as e:
        print(json.dumps({"error": f"Error communicating with Ollama: {str(e)}"}))
        sys.exit(1)

# Example Usage (Run from Node.js)
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No image path provided"}))
        sys.exit(1)

    image_path = sys.argv[1]  # Get image path from command-line arguments
    prompt = "Fetch invoice info from this image and respond in JSON form"

    query_ollama_with_image(image_path, prompt)
