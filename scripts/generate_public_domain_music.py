#!/usr/bin/env python3
import argparse
import json
import math
import struct
import subprocess
import wave
from pathlib import Path

SAMPLE_RATE = 22050
VOLUME = 0.28

NOTE_FREQ = {
    "C4": 261.63, "D4": 293.66, "E4": 329.63, "F4": 349.23,
    "G4": 392.00, "A4": 440.00, "B4": 493.88,
    "C5": 523.25, "D5": 587.33, "E5": 659.25, "F5": 698.46,
    "G5": 783.99, "A5": 880.00
}

SONGS = [
    {
        "id": "twinkle-twinkle",
        "title": "Twinkle Twinkle Little Star",
        "artist": "Public-domain melody • generated in GitHub Actions",
        "bpm": 105,
        "notes": [
            ("C4",1),("C4",1),("G4",1),("G4",1),("A4",1),("A4",1),("G4",2),
            ("F4",1),("F4",1),("E4",1),("E4",1),("D4",1),("D4",1),("C4",2),
            ("G4",1),("G4",1),("F4",1),("F4",1),("E4",1),("E4",1),("D4",2),
            ("G4",1),("G4",1),("F4",1),("F4",1),("E4",1),("E4",1),("D4",2),
            ("C4",1),("C4",1),("G4",1),("G4",1),("A4",1),("A4",1),("G4",2),
            ("F4",1),("F4",1),("E4",1),("E4",1),("D4",1),("D4",1),("C4",2)
        ]
    },
    {
        "id": "mary-little-lamb",
        "title": "Mary Had a Little Lamb",
        "artist": "Public-domain melody • generated in GitHub Actions",
        "bpm": 115,
        "notes": [
            ("E4",1),("D4",1),("C4",1),("D4",1),("E4",1),("E4",1),("E4",2),
            ("D4",1),("D4",1),("D4",2),("E4",1),("G4",1),("G4",2),
            ("E4",1),("D4",1),("C4",1),("D4",1),("E4",1),("E4",1),("E4",1),("E4",1),
            ("D4",1),("D4",1),("E4",1),("D4",1),("C4",2)
        ]
    },
    {
        "id": "row-row-row",
        "title": "Row, Row, Row Your Boat",
        "artist": "Public-domain melody • generated in GitHub Actions",
        "bpm": 112,
        "notes": [
            ("C4",1),("C4",1),("C4",1.5),("D4",0.5),("E4",1.5),("D4",0.5),("E4",1.5),("F4",0.5),
            ("G4",3),("C5",1),("C5",1),("C5",1),("G4",1),("G4",1),("G4",1),("E4",1),("E4",1),
            ("E4",1),("C4",1),("C4",1),("C4",1),("G4",1),("F4",0.5),("E4",0.5),("D4",0.5),("C4",2.5)
        ]
    },
    {
        "id": "frere-jacques",
        "title": "Frère Jacques",
        "artist": "Public-domain melody • generated in GitHub Actions",
        "bpm": 112,
        "notes": [
            ("C4",1),("D4",1),("E4",1),("C4",1),
            ("C4",1),("D4",1),("E4",1),("C4",1),
            ("E4",1),("F4",1),("G4",2),
            ("E4",1),("F4",1),("G4",2),
            ("G4",0.5),("A4",0.5),("G4",0.5),("F4",0.5),("E4",1),("C4",1),
            ("G4",0.5),("A4",0.5),("G4",0.5),("F4",0.5),("E4",1),("C4",1),
            ("C4",1),("G4",1),("C4",2),
            ("C4",1),("G4",1),("C4",2)
        ]
    },
    {
        "id": "london-bridge",
        "title": "London Bridge Is Falling Down",
        "artist": "Public-domain melody • generated in GitHub Actions",
        "bpm": 118,
        "notes": [
            ("G4",1),("A4",1),("G4",1),("F4",1),("E4",1),("F4",1),("G4",2),
            ("D4",1),("E4",1),("F4",2),("E4",1),("F4",1),("G4",2),
            ("G4",1),("A4",1),("G4",1),("F4",1),("E4",1),("F4",1),("G4",2),
            ("D4",2),("G4",2),("E4",1),("C4",2)
        ]
    },
    {
        "id": "old-macdonald",
        "title": "Old MacDonald Had a Farm",
        "artist": "Public-domain melody • generated in GitHub Actions",
        "bpm": 116,
        "notes": [
            ("C4",1),("C4",1),("C4",1),("G4",1),("A4",1),("A4",1),("G4",2),
            ("E4",1),("E4",1),("D4",1),("D4",1),("C4",2),
            ("G4",1),("C4",1),("C4",1),("C4",1),("G4",1),("A4",1),("A4",1),("G4",2),
            ("E4",1),("E4",1),("D4",1),("D4",1),("C4",2)
        ]
    },
    {
        "id": "baa-baa-black-sheep",
        "title": "Baa, Baa, Black Sheep",
        "artist": "Public-domain melody • generated in GitHub Actions",
        "bpm": 104,
        "notes": [
            ("C4",1),("C4",1),("G4",1),("G4",1),("A4",1),("A4",1),("G4",2),
            ("F4",1),("F4",1),("E4",1),("E4",1),("D4",1),("D4",1),("C4",2),
            ("G4",1),("G4",1),("F4",1),("F4",1),("E4",1),("E4",1),("D4",2),
            ("G4",1),("G4",1),("F4",1),("F4",1),("E4",1),("E4",1),("D4",2),
            ("C4",1),("C4",1),("G4",1),("G4",1),("A4",1),("A4",1),("G4",2),
            ("F4",1),("F4",1),("E4",1),("E4",1),("D4",1),("D4",1),("C4",2)
        ]
    }
]


def synth_note(freq, seconds):
    frames = bytearray()
    total = max(1, int(SAMPLE_RATE * seconds))
    attack = max(1, int(total * 0.04))
    release = max(1, int(total * 0.08))

    for i in range(total):
        env = 1.0
        if i < attack:
            env = i / attack
        elif i >= total - release:
            env = max(0.0, (total - i - 1) / release)

        t = i / SAMPLE_RATE
        # Soft, bell-like additive tone.
        sample = (
            math.sin(2 * math.pi * freq * t)
            + 0.22 * math.sin(2 * math.pi * freq * 2 * t)
            + 0.08 * math.sin(2 * math.pi * freq * 3 * t)
        )
        value = int(32767 * VOLUME * env * sample / 1.3)
        frames.extend(struct.pack("<h", max(-32768, min(32767, value))))

    return frames


def synth_silence(seconds):
    return b"\x00\x00" * int(SAMPLE_RATE * seconds)


def write_song_wav(song, wav_path):
    beat = 60.0 / song["bpm"]

    with wave.open(str(wav_path), "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(SAMPLE_RATE)

        # Play twice so each track feels complete.
        for _ in range(2):
            for note, beats in song["notes"]:
                seconds = beat * beats
                tone_seconds = max(0.05, seconds * 0.88)
                gap_seconds = max(0.01, seconds - tone_seconds)
                out.writeframes(synth_note(NOTE_FREQ[note], tone_seconds))
                out.writeframes(synth_silence(gap_seconds))
            out.writeframes(synth_silence(0.6))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--assets", default="app/src/main/assets")
    args = parser.parse_args()

    assets = Path(args.assets)
    audio_dir = assets / "audio"
    audio_dir.mkdir(parents=True, exist_ok=True)

    for p in audio_dir.glob("*.mp3"):
        p.unlink()

    manifest = []

    for song in SONGS:
        wav_path = audio_dir / f"{song['id']}.wav"
        mp3_path = audio_dir / f"{song['id']}.mp3"

        print(f"Generating {song['title']}...")
        write_song_wav(song, wav_path)

        subprocess.run(
            [
                "ffmpeg", "-y", "-loglevel", "error",
                "-i", str(wav_path),
                "-codec:a", "libmp3lame",
                "-b:a", "96k",
                str(mp3_path),
            ],
            check=True,
        )
        wav_path.unlink(missing_ok=True)

        manifest.append({
            "id": song["id"],
            "title": song["title"],
            "artist": song["artist"],
            "file": f"audio/{mp3_path.name}",
            "license": "Public-domain melody; CI-generated recording"
        })

    (assets / "songs.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    print(f"Generated {len(manifest)} tracks.")


if __name__ == "__main__":
    main()
