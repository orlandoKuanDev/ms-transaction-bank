package com.example.mstransaction.handler;

import com.example.mstransaction.exception.MethodArgumentNotValid;
import com.example.mstransaction.models.entities.Acquisition;
import com.example.mstransaction.models.entities.Bill;
import com.example.mstransaction.models.entities.Transaction;
import com.example.mstransaction.services.AcquisitionService;
import com.example.mstransaction.services.BillService;
import com.example.mstransaction.services.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j(topic = "TRANSACTION_HANDLER")
public class TransactionHandler {
    public static final Double COMMISSION_PER_TRANSACTION = 2.5;
    private final ITransactionService transactionService;
    private final BillService billService;
    private final AcquisitionService acquisitionService;

    @Autowired
    public TransactionHandler(ITransactionService transactionService, BillService billService, AcquisitionService acquisitionService) {
        this.transactionService = transactionService;
        this.billService = billService;
        this.acquisitionService = acquisitionService;
    }

    public Mono<ServerResponse> findAll(ServerRequest request){
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(transactionService.findAll(), Transaction.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request){
        String id = request.pathVariable("id");
        return errorHandler(
                transactionService.findById(id).flatMap(p -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> findByAccountNumber(ServerRequest request){
        String accountNumber = request.pathVariable("accountNumber");
        return errorHandler(
                billService.findByAccountNumber(accountNumber).flatMap(p -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> findByAcquisitionCardNumber(ServerRequest request){
        String cardNumber = request.pathVariable("cardNumber");
        return errorHandler(
                billService.findByCardNumber(cardNumber).flatMap(p -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> updateAcquisition(ServerRequest request){
        Mono<Acquisition> acquisition = request.bodyToMono(Acquisition.class);
        return acquisition.flatMap(acquisitionService::updateAcquisition).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByCardNumber(ServerRequest request){
        String cardNumber = request.pathVariable("cardNumber");
        return errorHandler(
                acquisitionService.findByCardNumber(cardNumber).flatMap(p -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> save(ServerRequest request){
        Mono<Transaction> transaction = request.bodyToMono(Transaction.class);
        Transaction newTransaction = new Transaction();
        Acquisition newAcquisition = new Acquisition();
        return transaction
               .flatMap(transaction1 -> {
                   newTransaction.setTransactionType(transaction1.getTransactionType());
                   newTransaction.setTransactionAmount(transaction1.getTransactionAmount());
                   newTransaction.setDescription(transaction1.getDescription());
                   newTransaction.setTransactionDate(LocalDateTime.now());
                   newTransaction.setBill(transaction1.getBill());
                   return acquisitionService.findByCardNumber(transaction1.getBill().getAcquisition().getCardNumber());
               })
               .flatMap(acquisition1 -> {
                   if (acquisition1
                           .getProduct()
                           .getRules()
                           .getMaximumLimitMonthlyMovementsQuantity() > 4){
                       newTransaction.setCommission(2.5);
                       newTransaction.getBill().setBalance(newTransaction.getBill().getBalance() - COMMISSION_PER_TRANSACTION);
                   }else{
                       newTransaction.setCommission(0.0);
                   }
                   newAcquisition.setProduct(acquisition1.getProduct());
                   newAcquisition.setCustomerHolder(acquisition1.getCustomerHolder());
                   newAcquisition.getProduct().getRules().setMaximumLimitMonthlyMovementsQuantity(acquisition1
                           .getProduct().getRules().getMaximumLimitMonthlyMovementsQuantity() + 1);
                   newAcquisition.setCustomerAuthorizedSigner(acquisition1.getCustomerAuthorizedSigner());
                   newAcquisition.setCardNumber(acquisition1.getCardNumber());
                   return acquisitionService.updateAcquisition(newAcquisition);
               }).flatMap(acquisition -> {
                   return billService.findByCardNumber(acquisition.getCardNumber()).flatMap(bill -> {
                       bill.setBalance(newTransaction.getBill().getBalance());
                       bill.setAcquisition(acquisition);
                       return billService.updateBill(bill);
                   });
               }).flatMap(bill -> {
                   newTransaction.setBill(bill);
                   return transactionService.create(newTransaction);
               }).flatMap(transactionResponse -> ServerResponse.created(URI.create("/api/transaction/".concat(newTransaction.getId())))
                       .contentType(MediaType.APPLICATION_JSON)
                       .bodyValue(newTransaction));
    }

    public Mono<ServerResponse> update(ServerRequest request){
        Mono<Transaction> product = request.bodyToMono(Transaction.class);
        String id = request.pathVariable("id");
        return errorHandler(
                product
                        .flatMap(p -> {
                            p.setId(id);
                            return transactionService.update(p);
                        })
                        .flatMap(p-> ServerResponse.created(URI.create("/api/product/".concat(p.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
        );
    }

    public Mono<ServerResponse> delete(ServerRequest request){
        String id = request.pathVariable("id");
        return errorHandler(
                transactionService.delete(id).then(ServerResponse.noContent().build())
        );
    }

    public Mono<ServerResponse> findAllByAccountNumber(ServerRequest request){
        String accountNumber = request.pathVariable("accountNumber");
        Mono<Bill> bill = billService.findByAccountNumber(accountNumber);
        return bill.flatMap(acc -> transactionService.findAll()
                .filter(list -> {
                    return list.getBill().getAccountNumber().equals(acc.getAccountNumber());
                })
                .collectList())
                .flatMap(list -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(list)
                .onErrorResume(e -> Mono.error(new MethodArgumentNotValid(
                        HttpStatus.BAD_REQUEST, String.format("The argument %s is not valid for this method", accountNumber), e)))
        );
	}

    /**
     *
     * @param request productName
     * @param request period
     * @param request dateInit
     * @return list of commission by product in range date
     */
    public Mono<ServerResponse> generateCommissionPerProductInRange(ServerRequest request){
        String productName = request.pathVariable("productName");
        String period = request.pathVariable("period");
        String dateInit = request.pathVariable("dateInit");
        LocalDate startDay =  LocalDate.parse(dateInit);
        LocalDate endDay = startDay.plusDays(Integer.parseInt(period));
        return transactionService.findByBill_Acquisition_Product_ProductName(productName)
                .flatMap(transaction ->
                        transactionService.findByTransactionDateBetween(startDay.atStartOfDay(), endDay.atStartOfDay())
                                .filter(pf -> Objects.equals(pf.getBill().getAcquisition().getProduct().getProductName(), transaction.getBill().getAcquisition().getProduct().getProductName())))
                .collectList()
                .flatMap(t -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(t))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public  Mono<ServerResponse> transactionBetweenDates(ServerRequest request){
        String periodDay = request.pathVariable("periodDay");
        LocalDate aDate = LocalDate.of(2021, 8, 12);
        LocalDate sixtyDaysBehind = aDate.plusDays(Integer.parseInt(periodDay));
        log.info("LIMIT_DATE: {}", sixtyDaysBehind);
        return transactionService.findByTransactionDateBetween(aDate.atStartOfDay(), sixtyDaysBehind.atStartOfDay())
                .collectList()
                .flatMap(t -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(t))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response){
        return response.onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if(errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "the transaction does not exist: ".concat(errorResponse.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", errorResponse.getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
            }
            return Mono.error(errorResponse);
        });
    }
}
