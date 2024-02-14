package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.entities.OrderPrimaryKey;
import com.ecommerce.app.EcommerceApp.entities.Orders;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.enums.PaymentStatus;
import com.ecommerce.app.EcommerceApp.exceptions.InvalidOrderDetailsException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.repositories.OrderRepository;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InvoiceGeneratorService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    public byte[] generateInvoice(String email,long orderId,long cartId){

        try {

            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
            Document document=new Document();
            PdfWriter.getInstance(document,outputStream);
            document.open();

            String title="Invoice Details";
            Font titleFont=new Font(Font.FontFamily.HELVETICA,22,Font.BOLD, BaseColor.RED);
            document.add(new Paragraph(title,titleFont));

            document.add(new Paragraph(generateInvoiceContext(email,orderId,cartId)));

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

    }

    private String generateInvoiceContext(String email,long orderId,long cartId){

        Orders order=orderRepository.findByOrderKeyId(new OrderPrimaryKey(orderId,cartId))
                .orElseThrow(()->new InvalidOrderDetailsException("Order with id : "+orderId+" not found"));
        StringBuilder invoiceContext = new StringBuilder();
        if(Objects.equals(order.getPaymentStatus(), PaymentStatus.CASH_ON_DELIVERY.name())
                || Objects.equals(order.getPaymentStatus(), PaymentStatus.PAYED.name())) {

            UserInfo userInfo = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("No user found with username : " + email));

            ProductDetails productDetails = order.getProductDetails();
            if (productDetails == null) {
                throw new ProductNotFoundException("product not found");
            }
            double totalAmount = order.getQuantity() * productDetails.getPrice();
            invoiceContext.append("Order Id     \t: \t").append(order.getOrderKeyId().getOrderId()).append("\n");
            invoiceContext.append("User         \t: \t").append(userInfo.getName()).append("\n");
            invoiceContext.append("User Email   \t: \t").append(userInfo.getEmail()).append("\n");
            invoiceContext.append("Product Name \t: \t").append(productDetails.getName()).append("\n");
            invoiceContext.append("Quantity     \t: \t").append(order.getQuantity()).append("\n");
            invoiceContext.append("Unit price   \t: \t").append(order.getProductDetails().getPrice()).append("\n");
            invoiceContext.append("Total Amount \t: \t").append(totalAmount).append("\n");
            return invoiceContext.toString();
        }
        return "You don't have any order placed";
    }


    public byte[] salesReportForAdmin(int startMont,int endMonth){
        try {

            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
            Document document=new Document();
            PdfWriter.getInstance(document,outputStream);
            document.open();

            String title="Monthly Sales Report";
            Font titleFont=new Font(Font.FontFamily.HELVETICA,22,Font.BOLD, BaseColor.RED);
            document.add(new Paragraph(title,titleFont));

            document.add(new Paragraph(getSalesInformation(startMont,endMonth)));

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSalesInformation(int startMonth,int endMonth) {
        List<Orders> allOrders=orderRepository.findByOrderDateTimeBetweenMonths(startMonth, endMonth);
        List<Orders> deliveredOrders=allOrders.stream()
                .filter(order-> Objects.equals(order.getStatus(), "DELIVERED"))
                .toList();
        if(!deliveredOrders.isEmpty()) {
            int totalSales = deliveredOrders.size();
            double totalProfit = deliveredOrders.stream()
                    .map(Orders::getTotalPrice)
                    .toList().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            Map<ProductDetails, Long> frequencyMap = deliveredOrders.stream()
                    .collect(Collectors.groupingBy(Orders::getProductDetails, Collectors.counting()));

            ProductDetails maxSelledProductDetails = Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            ProductDetails minSelledProductDetails = Collections.min(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();

            String bestSellingProductInfo = """
                            
                                                   name : %s
                                                   brand : %s
                                                   price : %.2f
                    """.formatted(maxSelledProductDetails.getName(), maxSelledProductDetails.getBrand(), maxSelledProductDetails.getPrice());
            String worstSellingProductInfo = """
                            
                                                   name : %s
                                                   brand : %s
                                                   price : %.2f
                    """.formatted(minSelledProductDetails.getName(), minSelledProductDetails.getBrand(), minSelledProductDetails.getPrice());

            StringBuilder salesReport = new StringBuilder();
            int totalNumberOfProductSold = deliveredOrders.stream().map(Orders::getQuantity).reduce(0, Integer::sum);

            salesReport.append("\nTotal sales                   : \t").append(totalSales).append("\n");
            salesReport.append("Total profit                  : \t").append(totalProfit).append("\n");
            salesReport.append("Total number of products sold : \t").append(totalNumberOfProductSold).append("\n");
            salesReport.append("\nBest selling product          : \t\n").append(bestSellingProductInfo).append("\n");
            if(!(frequencyMap.size()<2)) {
                salesReport.append("\nWorst selling product         : \t\n").append(worstSellingProductInfo).append("\n");
            }
            return salesReport.toString();
        }
        return "No order found..";
    }
}
