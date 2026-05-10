"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";

import { BellIcon, CloseIcon } from "./tailadmin-icons";

const notificationItems = [
  {
    id: "attendance",
    title: "Attendance belum lengkap",
    description: "Beberapa sesi hari ini belum ditandai attendance.",
    category: "Operations",
    time: "5 min ago",
    tone: "bg-orange-400",
  },
  {
    id: "assessment",
    title: "Assessment pending",
    description: "Ada murid aktif yang belum memiliki assessment terbaru.",
    category: "Reports",
    time: "15 min ago",
    tone: "bg-blue-500",
  },
  {
    id: "billing",
    title: "Invoice overdue",
    description: "Cek billing untuk pembayaran yang melewati due date.",
    category: "Billing",
    time: "1 hr ago",
    tone: "bg-rose-500",
  },
];

export function TailAdminNotificationDropdown() {
  const [isOpen, setIsOpen] = useState(false);
  const [notifying, setNotifying] = useState(true);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (!dropdownRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleClick = () => {
    setIsOpen((current) => !current);
    setNotifying(false);
  };

  return (
    <div ref={dropdownRef} className="relative">
      <button
        type="button"
        className="relative flex h-11 w-11 items-center justify-center rounded-full border border-gray-200 bg-white text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700"
        onClick={handleClick}
        aria-expanded={isOpen}
        aria-label="Open notifications"
      >
        <span className={`absolute right-0 top-0.5 z-10 h-2 w-2 rounded-full bg-orange-400 ${notifying ? "flex" : "hidden"}`}>
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-orange-400 opacity-75" />
        </span>
        <BellIcon className="h-5 w-5" />
      </button>

      {isOpen ? (
        <div className="absolute -right-[240px] mt-[17px] flex h-[430px] w-[350px] flex-col rounded-2xl border border-gray-200 bg-white p-3 shadow-lg sm:w-[361px] lg:right-0">
          <div className="mb-3 flex items-center justify-between border-b border-gray-100 pb-3">
            <h5 className="text-lg font-semibold text-gray-800">Notification</h5>
            <button
              type="button"
              onClick={() => setIsOpen(false)}
              className="text-gray-500 transition hover:text-gray-700"
              aria-label="Close notifications"
            >
              <CloseIcon className="h-6 w-6" />
            </button>
          </div>

          <ul className="flex h-auto flex-col overflow-y-auto">
            {notificationItems.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  onClick={() => setIsOpen(false)}
                  className="flex w-full gap-3 rounded-lg border-b border-gray-100 px-4 py-3 text-left hover:bg-gray-100"
                >
                  <span className="relative block h-10 w-10 shrink-0 rounded-full bg-gray-100">
                    <span className={`absolute bottom-0 right-0 z-10 h-2.5 w-2.5 rounded-full border-[1.5px] border-white ${item.tone}`} />
                    <span className="flex h-10 w-10 items-center justify-center rounded-full bg-gray-100 text-xs font-semibold text-gray-600">
                      {item.category.slice(0, 2).toUpperCase()}
                    </span>
                  </span>

                  <span className="block">
                    <span className="mb-1.5 block text-sm text-gray-500">
                      <span className="font-medium text-gray-800">{item.title}</span> {item.description}
                    </span>
                    <span className="flex items-center gap-2 text-xs text-gray-500">
                      <span>{item.category}</span>
                      <span className="h-1 w-1 rounded-full bg-gray-400" />
                      <span>{item.time}</span>
                    </span>
                  </span>
                </button>
              </li>
            ))}
          </ul>

          <Link
            href="/notification"
            onClick={() => setIsOpen(false)}
            className="mt-3 block rounded-lg border border-gray-300 bg-white px-4 py-2 text-center text-sm font-medium text-gray-700 hover:bg-gray-100"
          >
            View All Notifications
          </Link>
        </div>
      ) : null}
    </div>
  );
}
