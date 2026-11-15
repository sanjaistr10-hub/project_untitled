import { SiteEntry } from "@/components/site/MemberAuth";
import { RequestTimeMarker } from "@/components/site/RequestTimeMarker";

export default function SiteIndexPage() {
  return (
    <>
      <SiteEntry />
      <RequestTimeMarker />
    </>
  );
}
