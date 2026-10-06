#!/usr/bin/env python3
import argparse
import json
import os
import re
from pathlib import Path

from yt_dlp import YoutubeDL


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--sources", default="sources.json")
    parser.add_argument("--assets", default="app/src/main/assets")
    parser.add_argument("--cookies", default=None)
    return parser.parse_args()


def load_sources(path: Path):
    raw_input = os.getenv("INPUT_URLS", "").strip()

    if raw_input:
        urls = re.findall(r"https?://[^\s,]+", raw_input)
        return [{"enabled": True, "url": url} for url in urls]

    if not path.exists():
        return []

    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, list):
        raise ValueError("sources.json phải là một JSON array")

    return data


def main():
    args = parse_args()

    sources_path = Path(args.sources)
    assets_dir = Path(args.assets)
    audio_dir = assets_dir / "audio"
    manifest_path = assets_dir / "songs.json"

    audio_dir.mkdir(parents=True, exist_ok=True)
    assets_dir.mkdir(parents=True, exist_ok=True)

    # Mỗi workflow build từ đầu để APK không vô tình chứa audio cũ.
    for file in audio_dir.glob("*.mp3"):
        file.unlink()

    sources = [
        item for item in load_sources(sources_path)
        if item.get("enabled", True) and item.get("url")
    ]

    if not sources:
        manifest_path.write_text("[]\n", encoding="utf-8")
        print("Không có nguồn audio được bật. APK sẽ được build với danh sách trống.")
        return

    ydl_options = {
        "format": "bestaudio/best",
        "outtmpl": str(audio_dir / "%(id)s.%(ext)s"),
        "noplaylist": True,
        "retries": 5,
        "fragment_retries": 5,
        "continuedl": True,
        "overwrites": True,
        "postprocessors": [
            {
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
                "preferredquality": "128",
            }
        ],
    }

    if args.cookies:
        ydl_options["cookiefile"] = args.cookies

    songs = []
    failures = []

    with YoutubeDL(ydl_options) as ydl:
        for position, source in enumerate(sources, start=1):
            url = source["url"]
            print(f"\n[{position}/{len(sources)}] Đang xử lý: {url}")

            try:
                info = ydl.extract_info(url, download=True)
                media_id = str(info.get("id") or f"song-{position}")
                mp3_path = audio_dir / f"{media_id}.mp3"

                if not mp3_path.exists():
                    candidates = sorted(audio_dir.glob(f"{media_id}*.mp3"))
                    if not candidates:
                        raise FileNotFoundError(f"Không tìm thấy MP3 sau khi xử lý: {media_id}")
                    mp3_path = candidates[0]

                title = (source.get("title") or info.get("title") or f"Bài hát {position}").strip()
                artist = (
                    source.get("artist")
                    or info.get("artist")
                    or info.get("uploader")
                    or ""
                ).strip()

                songs.append(
                    {
                        "id": media_id,
                        "title": title,
                        "artist": artist,
                        "duration": info.get("duration"),
                        "file": f"audio/{mp3_path.name}",
                    }
                )

            except Exception as exc:
                failures.append({"url": url, "error": str(exc)})
                print(f"Không tải được: {url}\n{exc}")

    manifest_path.write_text(
        json.dumps(songs, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    print(f"\nĐã tạo {len(songs)} bài trong {manifest_path}")

    if failures:
        print("\nCác nguồn bị lỗi:")
        for failure in failures:
            print(f"- {failure['url']}: {failure['error']}")

    if not songs:
        raise SystemExit("Không có bài hát nào tải thành công; dừng build APK.")


if __name__ == "__main__":
    main()
