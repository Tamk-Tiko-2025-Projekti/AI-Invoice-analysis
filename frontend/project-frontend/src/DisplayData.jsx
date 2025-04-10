import React, { useState } from 'react';
import './DisplayData.css'

export default function DisplayData({ data }) {
  const [editableData, setEditableData] = useState(data.content);

  const handleInputChange = (e, field) => {
    setEditableData({
      ...editableData,
      [field]: e.target.value,
    });
  };

  const fields = [
    { label: 'Purchase_invoice_type_id', key: 'purchase_invoice_type_id' },
    { label: 'Purchase_supplier_id', key: 'purchase_supplier_id' },
    { label: 'Supplier_name', key: 'supplier_name' },
    { label: 'Invoice_date', key: 'invoice_date' },
    { label: 'Due_date', key: 'due_date' },
    { label: 'Bank_account', key: 'bank_account' },
    { label: 'Bank_bic', key: 'bank_bic' },
    { label: 'Total_gross', key: 'total_gross' },
    { label: 'Bank_reference', key: 'bank_reference' },
    { label: 'Bank_message', key: 'bank_message' },
    { label: 'Supplier_vat_number', key: 'supplier_vat_number' },
    { label: 'Supplier_business_id', key: 'supplier_business_id' },
    { label: 'Supplier_country', key: 'supplier_country' },
    { label: 'Invoice_number', key: 'invoice_number' },
    { label: 'Credited_invoice_no', key: 'credited_invoice_no' },
    { label: 'Discount_date1', key: 'discount_date1' },
    { label: 'Discount_percent1', key: 'discount_percent1' },
    { label: 'Discount_sum1', key: 'discount_sum1' },
    { label: 'Entry_date', key: 'entry_date' },
    { label: 'Total_net', key: 'total_net' },
    { label: 'Currency', key: 'currency' },
    { label: 'Order_number', key: 'order_number' },
    { label: 'Your_reference', key: 'your_reference' },
    { label: 'Our_reference', key: 'our_reference' },
    { label: 'Terms_of_payment', key: 'terms_of_payment' },
    { label: 'Payable_account_code', key: 'payable_account_code' },
  ];

  const requiredFields = [
    'purchase_invoice_type_id',
    'purchase_supplier_id',
    'supplier_name',
    'invoice_date',
    'due_date',
    'bank_account',
    'bank_bic',
    'total_gross',
  ];

  const validateFields = () => {
    let isValid = true;
    requiredFields.forEach((field) => {
      const value = editableData[field];
      if (value == null || value == "") {
        isValid = false;
      }
    });

    const bankReference = editableData['bank_reference'];
    const bankMessage = editableData['bank_message'];

    if ((bankReference && bankMessage) || (!bankReference && !bankMessage)) {
      alert("Either Bank Reference or Bank Message is required");
      isValid = false;
    }

    return isValid;
  }

  const handleSubmit = () => {
    if (validateFields()) {
      alert("All required fields are filled!");
    } else {
      alert("Please fill all the required fields!");
    }
  }

  return (
    <div>
      <h2>JSON data</h2>
      <div>
        {fields.map(({ label, key }) => (
          <label key={key}>
            {label}:
            <input
              type="text"
              value={editableData[key] || ''}
              size={(editableData[key]?.length || 1) + 1}
              onChange={(e) => handleInputChange(e, key)}
            />
          </label>
        ))}
      </div>
      <button onClick={handleSubmit}>Validate fields</button>
    </div>
  );
}