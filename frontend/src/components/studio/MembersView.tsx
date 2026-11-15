"use client";

import { useEffect, useState } from "react";
import { creatorApi } from "@/lib/api/creator";
import type { Member, PageResponse } from "@/lib/api/types";
import { formatDate, timeAgo } from "@/lib/format";
import { Avatar } from "@/components/ui/Avatar";
import { Button } from "@/components/ui/Button";
import { Icon } from "@/components/ui/icons";
import { Spinner } from "@/components/ui/Spinner";

const PAGE_SIZE = 20;

export function MembersView() {
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<PageResponse<Member> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    const timer = setTimeout(() => {
      setLoading(true);
      creatorApi
        .members({ q: query.trim() || undefined, page, size: PAGE_SIZE })
        .then((data) => !cancelled && setResult(data))
        .catch(() => !cancelled && setResult(null))
        .finally(() => !cancelled && setLoading(false));
    }, query ? 250 : 0);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [query, page]);

  return (
    <div className="flex flex-col gap-6 animate-fade-up">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Members</h1>
          <p className="mt-1 text-sm text-muted">
            Everyone who has signed up on your site{result ? ` · ${result.totalItems} total` : ""}.
          </p>
        </div>
        <label className="flex h-10 w-full items-center gap-2 rounded-lg border border-line bg-white px-3 focus-within:border-ink sm:w-72">
          <Icon name="search" className="size-4 text-muted" />
          <span className="sr-only">Search members</span>
          <input
            value={query}
            onChange={(event) => {
              setQuery(event.target.value);
              setPage(0);
            }}
            placeholder="Search name, email or username"
            className="h-full w-full bg-transparent text-sm outline-none placeholder:text-zinc-400"
          />
          {loading && <Spinner className="size-3.5 text-muted" />}
        </label>
      </header>

      <div className="overflow-hidden rounded-2xl border border-line bg-white">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead className="border-b border-line bg-surface/60 text-xs text-muted">
              <tr>
                <th scope="col" className="px-5 py-3 font-medium">Member</th>
                <th scope="col" className="px-5 py-3 font-medium">Email</th>
                <th scope="col" className="px-5 py-3 font-medium">Joined</th>
                <th scope="col" className="px-5 py-3 font-medium">Last active</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {result?.items.map((member) => (
                <tr key={member.id}>
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-3">
                      <Avatar name={member.fullName} seed={member.email} size="sm" />
                      <div>
                        <p className="font-medium">{member.fullName ?? "New member"}</p>
                        {member.username && <p className="text-xs text-muted">@{member.username}</p>}
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3 text-ink-soft">{member.email}</td>
                  <td className="px-5 py-3 text-ink-soft">{formatDate(member.joinedAt)}</td>
                  <td className="px-5 py-3 text-ink-soft">{timeAgo(member.lastLoginAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {result && result.items.length === 0 && (
          <p className="px-5 py-10 text-center text-sm text-muted">
            {query ? "No members match that search." : "No members yet. Share your site link to get your first sign-up."}
          </p>
        )}
      </div>

      {result && result.totalPages > 1 && (
        <div className="flex items-center justify-end gap-3 text-sm">
          <span className="text-muted">
            Page {result.page + 1} of {result.totalPages}
          </span>
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </Button>
          <Button variant="secondary" size="sm" disabled={page + 1 >= result.totalPages} onClick={() => setPage((p) => p + 1)}>
            Next
          </Button>
        </div>
      )}
    </div>
  );
}
