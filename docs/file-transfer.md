# Pass it on — file transfer design

QaRd is a **sender-only** "pass something on FAST" app: a link, a vCard, social
profiles, or a file. Everything the *receiver* touches is a single static QR opened with
their **stock phone camera** — no second app, no scanner, no account on the other end.

## Why a QR can't carry the file

A QR code holds ~3 KB at best, and even animated/streaming QR over standard codes tops out
around 10–30 kbps and *requires a custom receiver app* to film and reassemble the frames.
Since we want stock-camera receivers, anything that needs a custom receiver is out. So:

- **Link / vCard / social / text** → encoded directly in the QR (existing behaviour).
- **File** → the QR carries a **URL**; the app hosts the bytes and the receiver's browser
  downloads them.

## Routing a file: local when shared, cloud otherwise

| Case | Transport | Notes |
| --- | --- | --- |
| Both phones on the same Wi-Fi | In-app HTTP server, QR = `http://<ip>:<port>/d/<token>` | Fast, private, offline, free. |
| Not the same network | Upload to Cloudflare R2, QR = `https://…/d/<id>` | Works on any receiver network; needs sender data. |

Selection: try local (a site-local Wi-Fi IP is available) and fall back to cloud.

## Security model

The receiver is a stock browser, which **can't run any client-side decryption**, so the
server must return the real file. Confidentiality therefore rests on:

- an **unguessable token** in the URL path,
- the server only running **while the sender stays on the screen** (local), or a short
  **TTL** on the object (cloud),
- **HTTPS** for the cloud path.

(Client-side AES with the key in the QR was considered and rejected — it only works if our
own app is the receiver, which contradicts the stock-camera goal.)

## Cloudflare R2 + Worker (cloud path)

- `POST /new` → Worker returns a presigned R2 upload URL + a short download id; the client
  PUTs the file straight to R2.
- `GET /d/:id` → Worker streams the object back (or 302s to a presigned GET).
- An R2 lifecycle rule auto-deletes objects after the TTL; the Worker refuses expired ids.

Deploying this needs the repo owner's Cloudflare account (R2 bucket, Worker, API token);
the client code only does plain HTTPS upload/download, so it stays foss-clean.

## Phasing

- **Phase 0 (done):** Send flow + SAF file pick. *(No scanner anywhere.)*
- **Phase 1 (done):** Local-Wi-Fi serving + URL QR. `LocalFileServer` (NanoHTTPD) +
  `NetworkUtils` + `SendFileActivity`/`SendFileScreen`, launched from the editor's "Send"
  rail item. No backend; ships standalone.
- **Phase 2:** Cloudflare R2 relay (upload + short-link QR + expiry).
- **Phase 3:** Auto local/cloud selection + polish — foreground service so the local
  server survives backgrounding, retries, "link expires in…", progress.

## Components (Phase 1)

- `transfer/LocalFileServer.kt` — one-file NanoHTTPD server behind an unguessable token.
- `transfer/NetworkUtils.kt` — site-local IPv4 + Wi-Fi check.
- `ui/SendFileActivity.kt` — pick a file → start the server → show the URL QR.

## Known limitations (Phase 1)

- Same-Wi-Fi only (cloud relay is Phase 2).
- The server is tied to the Activity lifecycle; leaving the screen stops it. A foreground
  service (Phase 3) is needed for background-safe transfers of large files.
- No transfer progress UI yet.
