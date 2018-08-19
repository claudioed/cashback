package com.sensedia.cashback.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author claudioed on 19/08/18.
 * Project cashback
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterCashback {

  private String id;

  private String type;

  private String orderId;

  private Double orderTotal;

  private String userId;

  private String userEmail;

  private String storeName;

  private Double pointsEarned;

}
