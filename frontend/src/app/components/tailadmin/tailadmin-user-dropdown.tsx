"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";

import { LogoutIcon, SettingsIcon, UserCircleIcon } from "./tailadmin-icons";

interface TailAdminUserDropdownProps {
  username?: string | null;
  email?: string | null;
  onLogout: () => void;
}

export function TailAdminUserDropdown({ username, email, onLogout }: TailAdminUserDropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const displayName = username || "Unknown User";
  const userEmail = email || "No email";
  const initial = displayName.slice(0, 1).toUpperCase();

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (!dropdownRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div ref={dropdownRef} className="relative">
      <button
        type="button"
        onClick={(event) => {
          event.stopPropagation();
          setIsOpen((current) => !current);
        }}
        className="flex items-center text-gray-700 transition-colors hover:text-gray-900"
        aria-expanded={isOpen}
        aria-haspopup="menu"
      >
        <span className="mr-3 flex h-11 w-11 items-center justify-center overflow-hidden rounded-full bg-blue-50 text-sm font-semibold text-blue-700 ring-1 ring-blue-100">
          {initial}
        </span>
        <span className="mr-1 hidden font-medium text-sm lg:block">{displayName}</span>
        <svg
          className={`hidden stroke-gray-500 transition-transform duration-200 lg:block ${isOpen ? "rotate-180" : ""}`}
          width="18"
          height="20"
          viewBox="0 0 18 20"
          fill="none"
          aria-hidden="true"
        >
          <path d="M4.3125 8.65625L9 13.3437L13.6875 8.65625" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>

      {isOpen ? (
        <div className="absolute right-0 mt-[17px] flex w-[260px] flex-col rounded-2xl border border-gray-200 bg-white p-3 shadow-lg">
          <div>
            <span className="block text-sm font-medium text-gray-700">{displayName}</span>
            <span className="mt-0.5 block text-xs text-gray-500">{userEmail}</span>
          </div>

          <ul className="flex flex-col gap-1 border-b border-gray-200 py-4">
            <li>
              <Link
                href="/profile"
                onClick={() => setIsOpen(false)}
                className="group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-700"
              >
                <UserCircleIcon className="h-6 w-6 text-gray-500 group-hover:text-gray-700" />
                Edit profile
              </Link>
            </li>
            <li>
              <Link
                href="/profile"
                onClick={() => setIsOpen(false)}
                className="group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-700"
              >
                <SettingsIcon className="h-6 w-6 text-gray-500 group-hover:text-gray-700" />
                Account settings
              </Link>
            </li>
          </ul>

          <button
            type="button"
            onClick={() => {
              setIsOpen(false);
              onLogout();
            }}
            className="mt-3 flex items-center gap-3 rounded-lg px-3 py-2 text-left text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-700"
          >
            <LogoutIcon className="h-6 w-6 text-gray-500" />
            Sign out
          </button>
        </div>
      ) : null}
    </div>
  );
}
