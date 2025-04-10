import React, { useState } from 'react';
import './DisplayData.css'

export default function DisplayData({ data }) {
  const [editableData, setEditableData] = useState(data.content);
  const [errors, setErrors] = useState([]);

  const handleInputChange = (e, field) => {
    setEditableData({
      ...editableData,
      [field]: e.target.value,
    });
  };

  //All the possible fields for JSON
  const fields = [
    { label: 'Purchase invoice type id', key: 'purchase_invoice_type_id' },
    { label: 'Purchase supplier id', key: 'purchase_supplier_id' },
    { label: 'Supplier name', key: 'supplier_name' },
    { label: 'Invoice date', key: 'invoice_date' },
    { label: 'Due date', key: 'due_date' },
    { label: 'Bank account', key: 'bank_account' },
    { label: 'Bank bic', key: 'bank_bic' },
    { label: 'Total gross', key: 'total_gross' },
    { label: 'Bank reference', key: 'bank_reference' },
    { label: 'Bank message', key: 'bank_message' },
    { label: 'Supplier vat number', key: 'supplier_vat_number' },
    { label: 'Supplier business id', key: 'supplier_business_id' },
    { label: 'Supplier country', key: 'supplier_country' },
    { label: 'Invoice number', key: 'invoice_number' },
    { label: 'Credited invoice no', key: 'credited_invoice_no' },
    { label: 'Discount date1', key: 'discount_date1' },
    { label: 'Discount percent1', key: 'discount_percent1' },
    { label: 'Discount sum1', key: 'discount_sum1' },
    { label: 'Entry date', key: 'entry_date' },
    { label: 'Total net', key: 'total_net' },
    { label: 'Currency', key: 'currency' },
    { label: 'Order number', key: 'order_number' },
    { label: 'Your reference', key: 'your_reference' },
    { label: 'Our reference', key: 'our_reference' },
    { label: 'Terms of payment', key: 'terms_of_payment' },
    { label: 'Payable account code', key: 'payable_account_code' },
  ];

  //Required fields for Fennoa POST request (Bank Reference / - Message in validateFields function)
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
    const checkErrors = {};

    requiredFields.forEach((field) => {
      const value = editableData[field];
      if (value == null || value == "") {
        checkErrors[field] = `${field} is required!`
        isValid = false;
      }
    });

    const bankReference = editableData['bank_reference'];
    const bankMessage = editableData['bank_message'];

    if ((bankReference && bankMessage) || (!bankReference && !bankMessage)) {
      checkErrors['bank_reference'] = 'Either Bank Reference or Bank Message is required! Not both!';
      checkErrors['bank_message'] = 'Either Bank Reference or Bank Message is required! Not both!';
      isValid = false;
    }

    setErrors(checkErrors);
    return isValid;
  }

  const handleSubmit = () => {
    if (validateFields()) {
      alert("All required fields are filled!");
    } else {
      alert("Please fill all the required fields!" +
        errors.forEach((field) => {
          errors[field].toString();
        })
      );
    }
  }

  return (
    <div className='display-data'>
      <h2>JSON data</h2>
      <div className='fields-container'>
        {fields.map(({ label, key }) => (
          <label key={key}>
            {label}:
            <input
              type="text"
              value={editableData[key] || ''}
              size={(editableData[key]?.length || 1) + 1}
              onChange={(e) => handleInputChange(e, key)}
            />
            {errors[key] && <span style={{ color: 'red', fontSize: '0.9rem' }}>{errors[key]}</span>}
          </label>
        ))}
      </div>
      <button onClick={handleSubmit}>Validate fields (Submit)</button>
    </div>
  );
}
