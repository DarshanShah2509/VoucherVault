# VoucherVault
## Overview
The **VoucherVault** is a Spring Boot microservice designed for managing and applying discount coupons in an e-commerce platform. This service supports various coupon types such as:

- **Cart-wise Coupons**: Discounts on the entire cart if a minimum threshold is met.
- **Product-wise Coupons**: Discounts on specific products within the cart.
- **Buy X Get Y (BxGy) Coupons**: "Buy X items, get Y items free" deals, with configurable repetition limits.

## Features
- **CRUD Operations**: Create, read, update, and delete coupons.
- **Coupon Expiration**: Coupons have an expiration date set to 2 months from their creation. Expired coupons are automatically deactivated by a scheduled cron job.
- **Validation**: DTOs with validation for incoming requests.
- **Error Handling**: Graceful error handling for invalid inputs and expired coupons.
- **Unit Testing**: Comprehensive test cases using JUnit and Mockito.

## Tech Stack
- Java 17
- Spring Boot
- MongoDB
- Gradle
- JUnit & Mockito for testing
- Lombok for reducing boilerplate code

## API Endpoints

### Coupon Management
- **POST** `/api/v1/coupons`: Create a new coupon.
- **GET** `/api/v1/coupons`: Retrieve all coupons.
- **GET** `/api/v1/coupons/{id}`: Retrieve a specific coupon by ID.
- **PUT** `/api/v1/coupons/{id}`: Update a coupon by ID.
- **DELETE** `/api/v1/coupons/{id}`: Delete a coupon by ID.

### Coupon Application
- **POST** `/api/v1/coupons/applicable-coupons`: Get all applicable coupons for a given cart.
- **POST** `/api/v1/coupons/apply-coupon/{id}`: Apply a specific coupon to the cart.
  
## Coupon Expiration Feature
Each coupon has an expirationDate field, which is set to 2 months from the creation date.
A scheduled cron job runs every day at midnight to automatically deactivate expired coupons.

## Cron Job Configuration
- The cron job is defined in CouponExpirationScheduler.java and uses the following schedule:
  --0 0 0 * * ? — Runs every day at midnight.

## How to Customize the Expiration Cron Job
Modify the cron expression in CouponExpirationScheduler.java if you want to adjust the frequency.

## Assumptions
- Coupons are applied to the cart only if they are active and not expired.
- Cart-wise coupons are applied only if the cart total meets the specified threshold.
- Product-wise coupons are applied to specific products present in the cart.
- BxGy coupons are applied based on the quantities specified in the coupon details.

## Limitations
- No Coupon Stacking: Multiple coupons cannot be applied to the same cart.
- No Region-Based Coupons: The system does not support coupons limited to specific regions or user locations.
- No Time-Sensitive Coupons: Coupons valid only during specific times (e.g., flash sales) are not supported.
- No Complex Buy X Get Y Scenarios: Only straightforward "Buy X, Get Y" deals are supported.

## Edge Cases Handled
- Expired coupons are automatically deactivated and not considered during coupon application.
- Validation of input data to ensure correct formats and required fields.
- Handles cases where no applicable coupons are found for a given cart.

## Future Enhancements
- Coupon Stacking: Allow multiple coupons to be applied to a single cart.
- User-Specific Coupons: Support coupons limited to specific users or user groups.
- Category-Based Discounts: Apply discounts to all products within a specific category.
- Time-Based Coupons: Support coupons valid during specific hours (e.g., happy hour discounts).
- Geo-Location Based Coupons: Add support for location-based coupon restrictions.

