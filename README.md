# Baby Music

Ứng dụng Android nghe nhạc đơn giản cho trẻ nhỏ.

## Mục tiêu

- Giao diện chỉ gồm danh sách bài hát, chữ lớn, ít gây phân tâm.
- Chạm vào một bài để phát / tạm dừng.
- Không dùng iframe hoặc trình phát YouTube trong app.
- MP3 được tải trong GitHub Actions rồi đóng gói trực tiếp vào APK, nên app có thể phát offline.
- Repo không commit các file MP3 để tránh làm repo phình to.

## Luồng build

1. `sources.json` chứa 28 bài thiếu nhi (link YouTube lấy từ `catalog.json`), hoặc nhập URL khi chạy workflow thủ công. Chỉ dùng các URL media mà bạn **có quyền tải và sử dụng**.
2. GitHub Actions cài `yt-dlp` + Deno + `ffmpeg`.
3. Workflow trích audio thành MP3 128 kbps.
4. Script tạo `app/src/main/assets/songs.json` và bảng tóm tắt trong trang kết quả của run (tiêu đề + kênh YouTube thật của từng link, bài nào lỗi) để đối chiếu.
5. Gradle build APK Android.
6. APK và thư mục MP3 được upload thành GitHub Actions artifacts.

Workflow tự chạy khi push lên `main` có thay đổi `sources.json`, script hoặc app.

> Chỉ thêm các nguồn mà bạn có quyền tải, sao chép và sử dụng. Một số video YouTube có điều khoản hoặc bản quyền không cho phép tải/re-upload.

## Chạy workflow

Vào:

`Actions -> Build Baby Music APK -> Run workflow`

Có 3 cách cấp nguồn:

- Để trống ô URL: workflow tải 28 bài trong `sources.json`.
- Dán một hoặc nhiều URL được phép sử dụng vào ô URL. Có thể cách nhau bằng dòng mới hoặc khoảng trắng.
- Tick `builtin_music`: bỏ qua YouTube, build bằng 7 bài nursery public-domain do `scripts/generate_public_domain_music.py` tạo.

### Cookies YouTube (bắt buộc trên thực tế)

YouTube chặn IP runner của GitHub ("Sign in to confirm you're not a bot"), nên workflow cần cookies của một tài khoản YouTube:

1. Nên dùng một tài khoản Google phụ.
2. Mở cửa sổ ẩn danh, đăng nhập YouTube, rồi mở `https://www.youtube.com/robots.txt` trong cùng tab.
3. Dùng extension xuất cookies dạng Netscape (ví dụ "Get cookies.txt LOCALLY") để lưu cookies `youtube.com` thành `cookies.txt`, rồi đóng cửa sổ ẩn danh (không mở lại phiên đó để cookies không bị đổi).
4. Mã hoá base64:
   - macOS: `base64 -i cookies.txt | pbcopy`
   - Linux: `base64 -w0 cookies.txt`
   - Windows PowerShell (mở trong thư mục chứa file): `[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\cookies.txt")) | Set-Clipboard`
5. Repo → `Settings -> Secrets and variables -> Actions -> New repository secret`, tên `YOUTUBE_COOKIES_B64`, dán chuỗi base64.

Cookies hết hạn thì xuất lại và cập nhật secret.

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
    "artist": "Tên ca sĩ / kênh",
    "emoji": "🦆"
  }
]
```

Nếu bỏ `title` hoặc `artist`, script sẽ dùng metadata mà yt-dlp đọc được. `emoji` (tuỳ chọn) hiển thị cạnh tên bài trong app. Đặt `"enabled": false` để tạm bỏ một bài.

## Quyền riêng tư / trẻ em

APK hiện không yêu cầu quyền Internet. Audio nằm trong APK nên khi trẻ sử dụng sẽ không có quảng cáo, video đề xuất hay nút dẫn sang YouTube.
