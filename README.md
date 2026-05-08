# demo

## AWS Lambda: SES bounce -> webhook

This repository now includes `lambda_function.py`, an AWS Lambda handler that:

- Accepts SES bounce notifications (SNS-wrapped or direct event payloads)
- Captures all bounced email addresses in each bounce event
- Sends the bounce details to your webhook endpoint via `POST`

### Environment variables

- `WEBHOOK_URL` (required): webhook endpoint URL
- `WEBHOOK_TIMEOUT_SECONDS` (optional, default `10`)
- `WEBHOOK_AUTH_HEADER` (optional): custom auth header name (example: `Authorization`)
- `WEBHOOK_AUTH_VALUE` (optional): custom auth header value (example: `Bearer <token>`)

### Lambda handler

Use:

```
lambda_function.lambda_handler
```

### Recommended AWS wiring

1. In Amazon SES, configure an identity notification topic for **Bounce** events.
2. Subscribe this Lambda function to the SNS topic.
3. Set the required environment variables in Lambda.
4. Send a test email to an address that will hard-bounce and verify your webhook receives the payload.
