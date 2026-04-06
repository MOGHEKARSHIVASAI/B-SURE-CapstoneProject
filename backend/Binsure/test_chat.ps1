$email = "testbot5@hartford.com"
$pwd = "password123"

# 1. Register (ignore if already exists)
try {
    Invoke-RestMethod -Uri "http://localhost:8050/api/v1/auth/register" -Method Post -ContentType "application/json" -Body "{`"firstName`": `"Test`", `"lastName`": `"Bot`", `"email`": `"$email`", `"password`": `"$pwd`", `"phoneNumber`": `"1234567890`", `"role`": `"CUSTOMER`"}" | Out-Null
} catch {}

# 2. Login
$loginResp = Invoke-RestMethod -Uri "http://localhost:8050/api/v1/auth/login" -Method Post -ContentType "application/json" -Body "{`"email`": `"$email`", `"password`": `"$pwd`"}"
$token = $loginResp.token

if (-not $token) {
    Write-Output "Login failed"
    exit 1
}

# 3. Chat
try {
    $chatResp = Invoke-RestMethod -Uri "http://localhost:8050/api/chatbot/ask" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body "{`"message`": `"What is bsure?`"}"
    Write-Output $chatResp
} catch {
    Write-Output $_.Exception.Message
    $responseStream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($responseStream)
    Write-Output $reader.ReadToEnd()
}
