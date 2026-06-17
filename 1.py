

import socket
import threading
import argparse
import sys
from datetime import datetime

# ─── Color codes for terminal output ───────────────────────────────────────────
GREEN  = "\033[92m"
RED    = "\033[91m"
YELLOW = "\033[93m"
CYAN   = "\033[96m"
RESET  = "\033[0m"

# ─── Common ports with service names ───────────────────────────────────────────
COMMON_PORTS = {
    21:   "FTP",
    22:   "SSH",
    23:   "Telnet",
    25:   "SMTP",
    53:   "DNS",
    80:   "HTTP",
    110:  "POP3",
    135:  "MSRPC",
    139:  "NetBIOS",
    143:  "IMAP",
    443:  "HTTPS",
    445:  "SMB",
    3306: "MySQL",
    3389: "RDP",
    5432: "PostgreSQL",
    5900: "VNC",
    6379: "Redis",
    8080: "HTTP-Alt",
    8443: "HTTPS-Alt",
    27017:"MongoDB",
}

open_ports   = []
lock         = threading.Lock()


def resolve_target(target: str) -> str:
    """Resolve hostname to IP address."""
    try:
        ip = socket.gethostbyname(target)
        return ip
    except socket.gaierror:
        print(f"{RED}[!] Could not resolve host: {target}{RESET}")
        sys.exit(1)


def grab_banner(ip: str, port: int, timeout: float) -> str:
    """Try to grab a service banner from an open port."""
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.settimeout(timeout)
            s.connect((ip, port))
            # Send a generic HTTP request; works for many services
            s.send(b"HEAD / HTTP/1.0\r\n\r\n")
            banner = s.recv(1024).decode(errors="ignore").strip()
            # Return only the first line
            return banner.splitlines()[0] if banner else ""
    except Exception:
        return ""


def scan_port(ip: str, port: int, timeout: float, grab: bool):
    """Scan a single port and record if open."""
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.settimeout(timeout)
            result = s.connect_ex((ip, port))   # 0 = open, non-zero = closed

            if result == 0:
                service = COMMON_PORTS.get(port, "Unknown")
                banner  = grab_banner(ip, port, timeout) if grab else ""

                with lock:
                    open_ports.append(port)
                    if banner:
                        print(f"  {GREEN}[+] Port {port:<6} OPEN   {service:<15}  {banner}{RESET}")
                    else:
                        print(f"  {GREEN}[+] Port {port:<6} OPEN   {service}{RESET}")
    except Exception:
        pass


def parse_ports(port_arg: str):
    """Parse port argument: single, range (80-443), or list (22,80,443)."""
    ports = []
    for part in port_arg.split(","):
        part = part.strip()
        if "-" in part:
            start, end = part.split("-")
            ports.extend(range(int(start), int(end) + 1))
        else:
            ports.append(int(part))
    return ports


def print_banner(target: str, ip: str, ports: list, threads: int):
    print(f"""
{CYAN}╔══════════════════════════════════════════════════╗
║           Python Port Scanner  v1.0              ║
║        For educational purposes only             ║
╚══════════════════════════════════════════════════╝{RESET}

  Target   : {target} ({ip})
  Ports    : {len(ports)} port(s) to scan
  Threads  : {threads}
  Started  : {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
  {"─" * 50}
""")


def main():
    parser = argparse.ArgumentParser(
        description="Python Port Scanner - Educational Tool",
        formatter_class=argparse.RawTextHelpFormatter
    )
    parser.add_argument("target",
        help="Target IP address or hostname (e.g. 192.168.1.1 or scanme.nmap.org)")
    parser.add_argument("-p", "--ports", default="1-1024",
        help="Ports to scan. Examples:\n  -p 80\n  -p 1-1024\n  -p 22,80,443,8080\n  (default: 1-1024)")
    parser.add_argument("-t", "--threads", type=int, default=100,
        help="Number of threads (default: 100)")
    parser.add_argument("--timeout", type=float, default=1.0,
        help="Socket timeout in seconds (default: 1.0)")
    parser.add_argument("--banner", action="store_true",
        help="Attempt to grab service banners")
    parser.add_argument("--common", action="store_true",
        help="Scan only common ports (top 20 well-known ports)")

    args = parser.parse_args()

    # Resolve target
    ip = resolve_target(args.target)

    # Determine port list
    if args.common:
        ports = list(COMMON_PORTS.keys())
    else:
        try:
            ports = parse_ports(args.ports)
        except ValueError:
            print(f"{RED}[!] Invalid port format. Use: 80 | 1-1024 | 22,80,443{RESET}")
            sys.exit(1)

    ports = [p for p in ports if 1 <= p <= 65535]   # Validate range

    print_banner(args.target, ip, ports, args.threads)

    start_time = datetime.now()

    # Thread-based scanning
    threads_list = []
    semaphore    = threading.Semaphore(args.threads)

    def worker(port):
        with semaphore:
            scan_port(ip, port, args.timeout, args.banner)

    for port in ports:
        t = threading.Thread(target=worker, args=(port,))
        threads_list.append(t)
        t.start()

    for t in threads_list:
        t.join()

    # ─── Summary ───────────────────────────────────────────────────────────────
    duration = datetime.now() - start_time
    print(f"""
  {"─" * 50}
  {YELLOW}Scan complete in {duration.total_seconds():.2f}s{RESET}
  Open ports found : {GREEN}{len(open_ports)}{RESET}
  {GREEN}{sorted(open_ports)}{RESET}
""")


if __name__ == "__main__":
    main()