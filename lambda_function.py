import json
import logging
import os
from typing import Any, Dict, List
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


logger = logging.getLogger()
logger.setLevel(logging.INFO)


WEBHOOK_URL = os.environ.get("WEBHOOK_URL", "").strip()
WEBHOOK_TIMEOUT_SECONDS = int(os.environ.get("WEBHOOK_TIMEOUT_SECONDS", "10"))
WEBHOOK_AUTH_HEADER = os.environ.get("WEBHOOK_AUTH_HEADER", "").strip()
WEBHOOK_AUTH_VALUE = os.environ.get("WEBHOOK_AUTH_VALUE", "").strip()


def lambda_handler(event: Dict[str, Any], context: Any) -> Dict[str, Any]:
    """Process SES bounce notifications and forward them to a webhook."""
    if not WEBHOOK_URL:
        raise ValueError("WEBHOOK_URL environment variable is required.")

    notifications = _extract_notifications(event)
    bounce_payloads = [_build_bounce_payload(item) for item in notifications if _is_bounce(item)]

    if not bounce_payloads:
        logger.info("No SES bounce notifications found in event.")
        return {
            "statusCode": 200,
            "body": json.dumps(
                {
                    "message": "No bounce notifications found.",
                    "recordsReceived": len(notifications),
                }
            ),
        }

    webhook_failures = []
    for payload in bounce_payloads:
        try:
            _post_to_webhook(payload)
        except Exception as exc:  # pragma: no cover - surfaced via lambda logs
            webhook_failures.append({"mailMessageId": payload["mail"].get("messageId"), "error": str(exc)})

    if webhook_failures:
        # Raise so AWS can retry the event instead of dropping failed notifications.
        raise RuntimeError(
            f"Failed to send {len(webhook_failures)} bounce notification(s) to webhook: "
            f"{json.dumps(webhook_failures)}"
        )

    return {
        "statusCode": 200,
        "body": json.dumps(
            {
                "message": "Bounce notifications delivered.",
                "bouncesDelivered": len(bounce_payloads),
            }
        ),
    }


def _extract_notifications(event: Dict[str, Any]) -> List[Dict[str, Any]]:
    notifications: List[Dict[str, Any]] = []

    records = event.get("Records")
    if isinstance(records, list):
        for record in records:
            if not isinstance(record, dict):
                continue

            sns_message = record.get("Sns", {}).get("Message")
            if isinstance(sns_message, str):
                parsed_message = _parse_json_message(sns_message)
                if parsed_message:
                    notifications.append(parsed_message)
                continue

            if "notificationType" in record:
                notifications.append(record)

    if not notifications and "Message" in event:
        maybe_message = event.get("Message")
        if isinstance(maybe_message, str):
            parsed_message = _parse_json_message(maybe_message)
            if parsed_message:
                notifications.append(parsed_message)

    if not notifications and "notificationType" in event:
        notifications.append(event)

    return notifications


def _parse_json_message(message: str) -> Dict[str, Any]:
    try:
        parsed = json.loads(message)
        if isinstance(parsed, dict):
            return parsed
    except json.JSONDecodeError:
        logger.warning("Skipping unparseable SNS message: %s", message[:300])
    return {}


def _is_bounce(notification: Dict[str, Any]) -> bool:
    return str(notification.get("notificationType", "")).lower() == "bounce"


def _build_bounce_payload(notification: Dict[str, Any]) -> Dict[str, Any]:
    mail = notification.get("mail", {}) if isinstance(notification.get("mail"), dict) else {}
    bounce = notification.get("bounce", {}) if isinstance(notification.get("bounce"), dict) else {}
    bounced_recipients = bounce.get("bouncedRecipients", [])
    bounced_emails = [
        recipient.get("emailAddress")
        for recipient in bounced_recipients
        if isinstance(recipient, dict) and recipient.get("emailAddress")
    ]

    return {
        "eventType": "ses.bounce",
        "notificationType": notification.get("notificationType"),
        "mail": {
            "messageId": mail.get("messageId"),
            "timestamp": mail.get("timestamp"),
            "source": mail.get("source"),
            "destination": mail.get("destination", []),
            "headersTruncated": mail.get("headersTruncated"),
        },
        "bounce": {
            "bounceType": bounce.get("bounceType"),
            "bounceSubType": bounce.get("bounceSubType"),
            "timestamp": bounce.get("timestamp"),
            "feedbackId": bounce.get("feedbackId"),
            "reportingMTA": bounce.get("reportingMTA"),
            "bouncedRecipients": bounced_recipients,
        },
        "bouncedEmails": bounced_emails,
        "rawNotification": notification,
    }


def _post_to_webhook(payload: Dict[str, Any]) -> None:
    request_body = json.dumps(payload).encode("utf-8")
    headers = {"Content-Type": "application/json"}
    if WEBHOOK_AUTH_HEADER and WEBHOOK_AUTH_VALUE:
        headers[WEBHOOK_AUTH_HEADER] = WEBHOOK_AUTH_VALUE

    request = Request(WEBHOOK_URL, data=request_body, headers=headers, method="POST")
    try:
        with urlopen(request, timeout=WEBHOOK_TIMEOUT_SECONDS) as response:
            status_code = response.getcode()
            if status_code >= 400:
                raise RuntimeError(f"Webhook returned HTTP {status_code}")
            logger.info(
                "Webhook accepted bounce payload. status=%s messageId=%s",
                status_code,
                payload["mail"].get("messageId"),
            )
    except HTTPError as exc:
        error_body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"Webhook HTTPError {exc.code}: {error_body[:500]}") from exc
    except URLError as exc:
        raise RuntimeError(f"Webhook URLError: {exc.reason}") from exc
