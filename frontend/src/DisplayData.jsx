import React, { useState } from 'react';
import './DisplayData.css'

export default function DisplayData({ data, fileNames }) {
  const [activeTab, setActiveTab] = useState(0);
  const [editableData, setEditableData] = useState(data.map(item => item.content));
  const errorData = data.map(item => item.error);

  /*
  In this function we handle the changes of text fields. First we assign our editableData into updatedData variable.
  Then we make changes to updatedData when we change text in the text fields. After that we set the updatedData
  into editableData.
   */
  const handleInputChange = (e, field, index) => {
    const updatedData = [...editableData];
    updatedData[index] = {
      ...updatedData[index],
      [field]: e.target.value,
    };
    setEditableData(updatedData);
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

  //Simple function to track on which tab we are
  const handleTabClick = (index) => {
    setActiveTab(index);
  };

  /*
  Returns the file tabs and conditionally returns either error container if there are
  any errors on that specific tab. If not then it shows the data that the AI was able to get
  from the file
  */
  return (
    <div className="display-data">
      <div className="tabs">
        {data.map((_, index) => (
          <button
            key={index}
            className={activeTab === index ? 'active-tab' : ''}
            onClick={() => handleTabClick(index)}
          >
            File {index + 1}
          </button>
        ))}
      </div>

      {errorData[activeTab] ? (
        <div className="error-container">
          <h2>{errorData[activeTab]}</h2>
        </div>
      ) : (
        <>
          <h2>{fileNames[activeTab]}</h2>
          <div className="fields-container">
            {fields.map(({ label, key }) => (
              <label key={key}>
                {label}:
                <input
                  type="text"
                  value={editableData[activeTab][key] || ''}
                  size={(editableData[activeTab][key]?.length || 1) + 1}
                  onChange={(e) => handleInputChange(e, key, activeTab)}
                />
              </label>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
