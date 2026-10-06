# Baby Music

Ứng dụng Android nghe nhạc đơn giản cho trẻ nhỏ.

## Mục tiêu

- Giao diện chỉ gồm danh sách bài hát, chữ lớn, ít gây phân tâm.
- Chạm vào một bài để phát / tạm dừng.
- Không dùng iframe hoặc trình phát YouTube trong app.
- MP3 được tải trong GitHub Actions rồi đóng gói trực tiếp vào APK, nên app có thể phát offline.
- Repo không commit các file MP3 để tránh làm repo phình to.

## Luồng build

1. Thêm các URL media mà bạn **có quyền tải và sử dụng** vào `sources.json`, hoặc nhập URL khi chạy workflow thủ công.
2. GitHub Actions cài `yt-dlp` + `ffmpeg`.
3. Workflow trích audio thành MP3 128 kbps.
4. Script tạo `app/src/main/assets/songs.json`.
5. Gradle build APK Android.
6. APK và thư mục MP3 được upload thành GitHub Actions artifacts.

> Chỉ thêm các nguồn mà bạn có quyền tải, sao chép và sử dụng. Một số video YouTube có điều khoản hoặc bản quyền không cho phép tải/re-upload.

## Chạy workflow

Vào:

`Actions -> Build Baby Music APK -> Run workflow`

Có 2 cách cấp nguồn:

- Để trống ô URL: workflow đọc `sources.json`.
- Dán một hoặc nhiều URL được phép sử dụng vào ô URL. Có thể cách nhau bằng dòng mới hoặc khoảng trắng.

Sau khi build xong, tải artifact:

- `baby-music-apk`: file APK cài lên Android.
- `baby-music-audio`: các MP3 đã tạo trong lần build đó.

## sources.json

Ví dụ:

```json
[
  {
    "enabled": true,
    "url": "https://www.youtube.com/watch?v=VIDEO_ID",
    "title": "Tên bài hát",
    "artist": "Tên ca sĩ / kênh"
  }
]
```

Nếu bỏ `title` hoặc `artist`, script sẽ dùng metadata mà yt-dlp đọc được.

## Quyền riêng tư / trẻ em

APK hiện không yêu cầu quyền Internet. Audio nằm trong APK nên khi trẻ sử dụng sẽ không có quảng cáo, video đề xuất hay nút dẫn sang YouTube.
