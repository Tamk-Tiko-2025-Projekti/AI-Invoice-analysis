import React from 'react';

export default function DisplayData({ data }) {
  const item = data.content;

  return (
    <div>
      <h2>JSON data</h2>
      <div>
        <h3>Tarvittavat kentät Fennoa post requestia varten:</h3>
        <p>Purchase_invoice_type_id: {item.purchase_invoice_type_id}</p>
        <p>Purchase_supplier_id (jos tyhjä fennoa luo uuden supplierin): {item.purchase_supplier_id}</p>
        <p>Supplier_name: {item.supplier_name}</p>
        <p>Invoice_date: {item.invoice_date}</p>
        <p>Due_date: {item.due_date}</p>
        <p>Bank_account: {item.bank_account}</p>
        <p>Bank_bic: {item.bank_bic}</p>
        <p>Total_gross: {item.total_gross}</p>
        <p>
          Bank_reference: {item.bank_reference} tai Bank_message: {item.bank_message}
        </p>
      </div>
      <div>
        <h3>Muu data laskusta:</h3>
        <p>Supplier_vat_number: {item.supplier_vat_number}</p>
        <p>Supplier_business_id: {item.supplier_business_id}</p>
        <p>supplier_country: {item.supplier_country}</p>
        <p>Invoice_number: {item.invoice_number}</p>
        <p>Credited_invoice_no: {item.credited_invoice_no}</p>
        <p>Discount_date1: {item.discount_date1}</p>
        <p>Discount_percent1: {item.discount_percent1}</p>
        <p>Discount_sum1: {item.discount_sum1}</p>
        <p>Entry_date: {item.entry_date}</p>
        <p>Total_net: {item.total_net}</p>
        <p>Currency: {item.currency}</p>
        <p>Order_number: {item.order_number}</p>
        <p>Your_reference: {item.your_reference}</p>
        <p>Our_reference: {item.our_reference}</p>
        <p>Terms_of_payment: {item.terms_of_payment}</p>
        <p>Payable_account_code: {item.payable_account_code}</p>
      </div>
    </div>
  );
}
