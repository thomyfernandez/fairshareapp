#!/usr/bin/env python3
"""Crea .env local sin sobrescribir configuración existente ni imprimir claves."""
from pathlib import Path
import secrets
root = Path(__file__).resolve().parent.parent
path = root / ".env"
if path.exists():
    print(".env ya existe; se conserva.")
else:
    path.write_text("JWT_SECRET=" + secrets.token_urlsafe(48) + "\nJWT_EXPIRATION_MS=3600000\nSPRING_PROFILES_ACTIVE=local\n", encoding="utf-8")
    path.chmod(0o600)
    print(".env generado. No compartir ni subir al repositorio.")
