package com.javabom.producercomsumer.producercomsumer.service;

import com.javabom.producercomsumer.producercomsumer.domain.Account;
import com.javabom.producercomsumer.producercomsumer.domain.AccountRepository;
import com.javabom.producercomsumer.producercomsumer.dto.CardPaymentRequestDto;
import com.javabom.producercomsumer.producercomsumer.event.CardPaymentEvent;
import com.javabom.producercomsumer.producercomsumer.event.EventBroker;
import com.javabom.producercomsumer.producercomsumer.support.PayVendor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardPaymentService {

    private final EventBroker<CardPaymentEvent> eventBroker;
    private final AccountRepository accountRepository;
    private final PayVendor cardPayVendor;

    public void requestPay(final CardPaymentRequestDto cardPaymentRequestDto) {
        eventBroker.offer(new CardPaymentEvent(cardPaymentRequestDto));
    }

    @Transactional
    public void pay(final CardPaymentEvent paymentEvent) {
        if (!successRequestPayToVendor(paymentEvent)) {
            recordFailToCardPayment(paymentEvent);
            return;
        }

        Account account = accountRepository.findAccountByUserId(paymentEvent.getCardPaymentRequestDto().getUserId())
                .orElseThrow(IllegalArgumentException::new);

        account.cardPay(paymentEvent.getCardPaymentRequestDto(), true);
    }

    private boolean successRequestPayToVendor(CardPaymentEvent paymentEvent) {
        return cardPayVendor.requestPayToVendor(paymentEvent);
    }

    private void recordFailToCardPayment(CardPaymentEvent paymentEvent) {
        Account account = accountRepository.findAccountByUserId(paymentEvent.getCardPaymentRequestDto().getUserId())
                .orElseThrow(IllegalArgumentException::new);
        account.cardPay(paymentEvent.getCardPaymentRequestDto(), false);
    }

}
