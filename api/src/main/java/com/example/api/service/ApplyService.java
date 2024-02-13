package com.example.api.service;


import com.example.api.producer.CouponCreateProducer;
import com.example.api.repository.AppliedUserRepository;
import com.example.api.repository.CouponCountRepository;
import com.example.api.repository.CouponRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplyService {

    private final CouponRepository couponRepository;

    private final CouponCountRepository couponCountRepository;

    private final CouponCreateProducer couponCreateProducer;

    private final AppliedUserRepository appliedUserRepository;

    public ApplyService(CouponRepository couponRepository, CouponCountRepository couponCountRepository, CouponCreateProducer couponCreateProducer, AppliedUserRepository appliedUserRepository) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
        this.couponCreateProducer = couponCreateProducer;
        this.appliedUserRepository = appliedUserRepository;
    }

    public void apply(Long userId) {
        /**
         * 레이스 컨디션
         * 2개 이상의 스레드에서 공유데이터에 엑세스를 할 때 발생하는 문제
         * 싱글 스레드로 작업한다면 문제가 발생하지 않음.
         * 성능 문제가 있기 때문에 싱글 스레드로 하면 안됨.
         *
         */

        // 쿠폰 데이터에 대한 정합성을 관리
        // redis incr key:value
//        valuelong count = couponRepository.count();

        // lock start
        // 쿠폰 발급 여부
        // if(발급됐다면) return

        Long apply = appliedUserRepository.add(userId);

        // 추가된 개수가 1이 아니라면 이 유저는 이미 발급 요청을 했던 유저라서 쿠폰을 발급하지 않고 리턴해줌.
        if ( apply != 1 ) {
            return;
        }
        Long count = couponCountRepository.increment();
        if( count > 100) {
            return;
        }

        //couponRepository.save(new Coupon(userId));

        couponCreateProducer.create(userId);


    }
}
