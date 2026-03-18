import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  styles: [`
    :host { display: block; font-family: 'Inter', sans-serif; color: #1A120E; }

    /* ── NAV ─────────────────────────────────────── */
    .nav {
      position: fixed; top: 0; left: 0; right: 0; z-index: 200;
      display: flex; align-items: center; justify-content: space-between;
      padding: 0 60px; height: 68px;
      background: rgba(26,10,14,0.88);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid rgba(196,100,120,0.18);
      transition: background 0.3s;
    }
    .nav-brand {
      font-family: 'Space Grotesk', sans-serif;
      font-size: 1.55rem; font-weight: 900; color: #FAF6F0;
      letter-spacing: -0.5px; text-decoration: none;
    }
    .nav-brand em { font-style: normal; color: #CC6678; }
    .nav-links { display: flex; gap: 28px; align-items: center; }
    .nav-links a {
      color: rgba(245,240,232,0.68); font-size: 0.88rem;
      text-decoration: none; transition: color 0.2s; font-weight: 500;
    }
    .nav-links a:hover { color: #FAF6F0; }
    .nav-signin {
      background: transparent; border: 1.5px solid rgba(245,240,232,0.3);
      color: #FAF6F0 !important; padding: 8px 20px; border-radius: 8px;
      font-weight: 600 !important;
    }
    .nav-signin:hover { border-color: #FAF6F0 !important; background: rgba(255,255,255,0.05) !important; }
    .nav-cta {
      background: linear-gradient(135deg, #982340, #5C1227) !important;
      color: #FAF6F0 !important; padding: 9px 22px; border-radius: 8px;
      font-weight: 700 !important;
      box-shadow: 0 4px 14px rgba(92,18,39,0.4);
    }
    .nav-cta:hover { box-shadow: 0 6px 20px rgba(92,18,39,0.5) !important; transform: translateY(-1px); }

    /* ── HERO ─────────────────────────────────────── */
    .hero {
      min-height: 100vh;
      position: relative;
      display: flex; align-items: center; justify-content: center;
      text-align: center; padding: 120px 24px 100px;
      overflow: hidden;
    }
    .hero-bg {
      position: absolute; inset: 0;
      background-image: url('/assets/hero-bg.png');
      background-size: cover; background-position: center center;
      background-repeat: no-repeat;
    }
    /* dark overlay so text reads clearly */
    .hero-overlay {
      position: absolute; inset: 0;
      background: linear-gradient(
        160deg,
        rgba(26,10,14,0.78) 0%,
        rgba(60,9,21,0.72) 40%,
        rgba(20,8,10,0.82) 100%
      );
    }
    /* bottom fade into stats bar */
    .hero-fade {
      position: absolute; bottom: 0; left: 0; right: 0; height: 160px;
      background: linear-gradient(to bottom, transparent, #FAF6F0);
    }
    .hero-content { position: relative; z-index: 2; max-width: 800px; }

    .hero-pill {
      display: inline-flex; align-items: center; gap: 8px;
      font-size: 0.72rem; letter-spacing: 3.5px; text-transform: uppercase;
      color: #E0A0AE; font-weight: 700;
      border: 1px solid rgba(224,160,174,0.35);
      padding: 6px 20px; border-radius: 999px; margin-bottom: 32px;
      background: rgba(152,35,64,0.12);
    }
    .hero-pill span { width: 6px; height: 6px; border-radius: 50%; background: #CC6678; display: inline-block; }

    .hero h1 {
      font-family: 'Space Grotesk', sans-serif;
      font-size: clamp(2.8rem, 6.5vw, 5.2rem);
      font-weight: 800; line-height: 1.06; letter-spacing: -1.5px;
      color: #FAF6F0; margin-bottom: 26px;
    }
    .hero h1 em { font-style: italic; color: #E0A0AE; font-family: 'DM Serif Display', serif; }
    .hero-sub {
      font-size: 1.15rem; color: rgba(245,240,232,0.72);
      line-height: 1.75; max-width: 560px; margin: 0 auto 44px;
    }
    .hero-btns { display: flex; gap: 14px; justify-content: center; flex-wrap: wrap; }

    .btn-hero-primary {
      background: linear-gradient(135deg, #982340, #5C1227);
      color: #FAF6F0; padding: 15px 36px; border-radius: 10px;
      font-family: 'Space Grotesk', sans-serif; font-size: 0.95rem; font-weight: 700;
      text-decoration: none; transition: all 0.2s; display: inline-flex; align-items: center; gap: 8px;
      box-shadow: 0 6px 22px rgba(92,18,39,0.45);
    }
    .btn-hero-primary:hover { transform: translateY(-2px); box-shadow: 0 10px 30px rgba(92,18,39,0.55); }
    .btn-hero-outline {
      background: rgba(255,255,255,0.08); color: #FAF6F0;
      padding: 15px 36px; border-radius: 10px;
      border: 1.5px solid rgba(245,240,232,0.35);
      font-family: 'Space Grotesk', sans-serif; font-size: 0.95rem; font-weight: 600;
      text-decoration: none; transition: all 0.2s; display: inline-flex; align-items: center; gap: 8px;
    }
    .btn-hero-outline:hover { border-color: rgba(245,240,232,0.7); background: rgba(255,255,255,0.12); }

    /* Trust row */
    .hero-trust {
      position: relative; z-index: 2;
      display: flex; align-items: center; justify-content: center; gap: 24px;
      margin-top: 52px; flex-wrap: wrap;
    }
    .trust-item {
      display: flex; align-items: center; gap: 7px;
      color: rgba(245,240,232,0.55); font-size: 0.8rem; font-weight: 500;
    }
    .trust-item span { color: #CC6678; font-size: 1rem; }
    .trust-divider { width: 1px; height: 16px; background: rgba(255,255,255,0.15); }

    /* Scroll hint */
    .hero-scroll {
      position: absolute; bottom: 32px; left: 50%; transform: translateX(-50%);
      z-index: 2; color: rgba(245,240,232,0.35); font-size: 0.68rem;
      letter-spacing: 2.5px; text-transform: uppercase;
      display: flex; flex-direction: column; align-items: center; gap: 8px;
    }
    .scroll-line {
      width: 1px; height: 36px;
      background: linear-gradient(to bottom, rgba(245,240,232,0.4), transparent);
      animation: scrollPulse 2s ease-in-out infinite;
    }
    @keyframes scrollPulse {
      0%, 100% { opacity: 0.3; } 50% { opacity: 1; }
    }

    /* ── STATS BAR ────────────────────────────────── */
    .stats-bar {
      background: #FAF6F0; border-bottom: 1px solid #EAE0D8;
      padding: 48px 60px;
      display: grid; grid-template-columns: repeat(4, 1fr);
    }
    .stat-item {
      text-align: center; padding: 0 28px;
      border-right: 1px solid #EAE0D8;
    }
    .stat-item:last-child { border-right: none; }
    .stat-num {
      font-family: 'Space Grotesk', sans-serif;
      font-size: 2.5rem; font-weight: 800; color: #5C1227; display: block;
      letter-spacing: -1px;
    }
    .stat-lbl { font-size: 0.8rem; color: #A8958A; letter-spacing: 0.5px; margin-top: 5px; }

    /* ── SECTIONS SHARED ─────────────────────────── */
    section { padding: 96px 60px; }
    .section-eyebrow {
      font-size: 0.68rem; letter-spacing: 4px; text-transform: uppercase;
      color: #982340; font-weight: 700; margin-bottom: 14px; display: block;
    }
    .section-title {
      font-family: 'Space Grotesk', sans-serif;
      font-size: clamp(1.9rem, 3vw, 2.7rem); font-weight: 800;
      color: #1A120E; line-height: 1.12; margin-bottom: 16px; letter-spacing: -0.5px;
    }
    .section-sub { font-size: 1.05rem; color: #8C7A70; max-width: 520px; line-height: 1.75; }
    .section-tag { color: #7A1C35; }

    /* ── PRODUCTS ────────────────────────────────── */
    .products-section { background: #FAF6F0; }
    .products-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 52px; }
    .products-cta { white-space: nowrap; }
    .products-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
    .product-card {
      background: #FFFFFF; border: 1px solid #EAE0D8; border-radius: 14px;
      padding: 32px 28px; transition: all 0.25s; position: relative; overflow: hidden;
    }
    .product-card::before {
      content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px;
      background: linear-gradient(90deg, #B83B57, #5C1227);
      transform: scaleX(0); transition: transform 0.25s; transform-origin: left;
    }
    .product-card:hover { border-color: #E0A0AE; transform: translateY(-4px); box-shadow: 0 14px 36px rgba(92,18,39,0.1); }
    .product-card:hover::before { transform: scaleX(1); }

    .product-icon-wrap {
      width: 46px; height: 46px; border-radius: 12px;
      background: #F2D5DB; display: flex; align-items: center; justify-content: center;
      margin-bottom: 18px;
    }
    .product-card h3 {
      font-family: 'Space Grotesk', sans-serif; font-size: 1.05rem;
      font-weight: 700; color: #1A120E; margin-bottom: 10px;
    }
    .product-card p { font-size: 0.87rem; color: #8C7A70; line-height: 1.65; margin-bottom: 18px; }
    .product-tag {
      display: inline-block; font-size: 0.65rem; letter-spacing: 2px;
      text-transform: uppercase; font-weight: 700;
      background: #F2D5DB; color: #7A1C35; padding: 3px 11px; border-radius: 999px;
    }

    /* ── HOW IT WORKS ────────────────────────────── */
    .how-section { background: #F3EDE5; }
    .how-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 80px; align-items: center; }
    .how-steps { display: flex; flex-direction: column; }
    .step {
      display: flex; gap: 22px; padding: 26px 0; border-bottom: 1px solid #D8CAC0;
      transition: background 0.2s; cursor: default;
    }
    .step:last-child { border-bottom: none; }
    .step-num {
      font-family: 'Space Grotesk', sans-serif;
      font-size: 1.8rem; font-weight: 800; color: #D8CAC0;
      line-height: 1; min-width: 44px; transition: color 0.2s;
    }
    .step:hover .step-num { color: #7A1C35; }
    .step-content h4 {
      font-family: 'Space Grotesk', sans-serif;
      font-size: 1rem; font-weight: 700; color: #1A120E; margin-bottom: 6px;
    }
    .step-content p { font-size: 0.875rem; color: #8C7A70; line-height: 1.65; }

    /* Policy preview card */
    .how-visual {
      background: linear-gradient(145deg, #5C1227, #3B0915);
      border-radius: 16px; padding: 44px 40px; color: #FAF6F0;
      box-shadow: 0 24px 60px rgba(26,10,14,0.35);
      position: relative; overflow: hidden;
    }
    .how-visual::before {
      content: ''; position: absolute; top: -40px; right: -40px;
      width: 160px; height: 160px; border-radius: 50%;
      background: rgba(184,59,87,0.18); pointer-events: none;
    }
    .how-visual-header {
      display: flex; align-items: center; gap: 12px; margin-bottom: 28px;
      padding-bottom: 20px; border-bottom: 1px solid rgba(245,240,232,0.1);
    }
    .visual-logo {
      width: 36px; height: 36px; border-radius: 10px;
      background: rgba(245,240,232,0.12);
      display: flex; align-items: center; justify-content: center; font-size: 1.1rem;
    }
    .visual-title { font-family: 'Space Grotesk', sans-serif; font-size: 1.1rem; font-weight: 700; }
    .visual-subtitle { font-size: 0.72rem; color: rgba(245,240,232,0.45); margin-top: 2px; }

    .visual-stat { margin-bottom: 24px; }
    .visual-stat-label {
      font-size: 0.67rem; letter-spacing: 2px; text-transform: uppercase;
      color: #E0A0AE; font-weight: 700;
    }
    .visual-stat-value {
      font-family: 'Space Grotesk', sans-serif; font-size: 1.75rem;
      font-weight: 800; margin: 4px 0 10px;
    }
    .visual-bar { height: 5px; background: rgba(245,240,232,0.12); border-radius: 3px; overflow: hidden; }
    .visual-fill { height: 100%; border-radius: 3px; background: linear-gradient(90deg, #CC6678, #E0A0AE); }
    .fill-75 { width: 75%; } .fill-60 { width: 60%; } .fill-88 { width: 88%; }

    .next-premium-wrap {
      margin-top: 28px; padding-top: 24px;
      border-top: 1px solid rgba(245,240,232,0.1);
      display: flex; justify-content: space-between; align-items: flex-end;
    }
    .next-premium-label { font-size: 0.7rem; color: rgba(245,240,232,0.45); text-transform: uppercase; letter-spacing: 2px; margin-bottom: 4px; }
    .next-premium-date { font-family: 'Space Grotesk', sans-serif; font-size: 1.15rem; font-weight: 700; }
    .active-chip {
      display: inline-flex; align-items: center; gap: 5px;
      background: rgba(110,175,132,0.18); color: #9ECE9E; font-size: 0.68rem;
      font-weight: 700; letter-spacing: 1px; padding: 4px 10px; border-radius: 999px; text-transform: uppercase;
    }

    /* ── TESTIMONIALS ────────────────────────────── */
    .testimonials-section { background: #FAF6F0; }
    .testimonials-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-top: 52px; }
    .testimonial-card {
      background: #FFFFFF; border: 1px solid #EAE0D8; border-radius: 14px;
      padding: 30px 26px; transition: all 0.2s;
    }
    .testimonial-card:hover { transform: translateY(-3px); box-shadow: 0 12px 32px rgba(92,18,39,0.08); border-color: #D8CAC0; }
    .stars { color: #CA6068; font-size: 0.9rem; margin-bottom: 14px; letter-spacing: 2px; }
    .testimonial-quote {
      font-size: 0.9rem; color: #2D1E18; line-height: 1.8;
      margin-bottom: 22px; font-style: italic;
    }
    .testimonial-author { display: flex; align-items: center; gap: 12px; }
    .author-avatar {
      width: 40px; height: 40px; border-radius: 10px;
      background: linear-gradient(135deg, #982340, #5C1227); color: #FAF6F0;
      display: flex; align-items: center; justify-content: center;
      font-family: 'Space Grotesk', sans-serif; font-size: 0.85rem; font-weight: 700;
      flex-shrink: 0;
    }
    .author-name { font-weight: 700; font-size: 0.875rem; color: #1A120E; }
    .author-company { font-size: 0.75rem; color: #A8958A; margin-top: 1px; }

    /* ── CTA ─────────────────────────────────────── */
    .cta-section {
      background: linear-gradient(135deg, #3B0915 0%, #7A1C35 100%);
      padding: 100px 60px; text-align: center; position: relative; overflow: hidden;
    }
    .cta-section::before {
      content: ''; position: absolute; inset: 0;
      background-image:
        linear-gradient(rgba(245,240,232,0.04) 1px, transparent 1px),
        linear-gradient(90deg, rgba(245,240,232,0.04) 1px, transparent 1px);
      background-size: 40px 40px;
    }
    .cta-content { position: relative; z-index: 1; }
    .cta-section h2 {
      font-family: 'Space Grotesk', sans-serif;
      font-size: clamp(2rem, 4.5vw, 3.2rem);
      font-weight: 800; color: #FAF6F0; margin-bottom: 18px; letter-spacing: -0.5px;
    }
    .cta-section p {
      font-size: 1.05rem; color: rgba(245,240,232,0.68);
      max-width: 460px; margin: 0 auto 44px; line-height: 1.75;
    }
    .cta-btns { display: flex; gap: 14px; justify-content: center; flex-wrap: wrap; }
    .btn-cta-primary {
      background: #FAF6F0; color: #3B0915; padding: 15px 38px; border-radius: 10px;
      font-family: 'Space Grotesk', sans-serif; font-size: 0.95rem; font-weight: 800;
      text-decoration: none; transition: all 0.2s; display: inline-flex; align-items: center; gap: 8px;
    }
    .btn-cta-primary:hover { background: #F3EDE5; transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.2); }
    .btn-cta-outline {
      background: rgba(245,240,232,0.08); color: #FAF6F0; padding: 15px 38px; border-radius: 10px;
      border: 1.5px solid rgba(245,240,232,0.3);
      font-family: 'Space Grotesk', sans-serif; font-size: 0.95rem; font-weight: 600;
      text-decoration: none; transition: all 0.2s; display: inline-flex; align-items: center; gap: 8px;
    }
    .btn-cta-outline:hover { border-color: rgba(245,240,232,0.7); background: rgba(255,255,255,0.12); }

    /* ── FOOTER ──────────────────────────────────── */
    footer {
      background: #1A120E; color: rgba(245,240,232,0.55);
      padding: 60px; display: grid;
      grid-template-columns: 2.2fr 1fr 1fr 1fr; gap: 48px;
    }
    .footer-brand h3 {
      font-family: 'Space Grotesk', sans-serif;
      font-size: 1.45rem; font-weight: 900; color: #FAF6F0; margin-bottom: 10px; letter-spacing: -0.5px;
    }
    .footer-brand h3 em { font-style: normal; color: #CC6678; }
    .footer-brand p { font-size: 0.84rem; line-height: 1.75; max-width: 300px; }
    .footer-col h4 {
      font-size: 0.65rem; letter-spacing: 3px; text-transform: uppercase;
      font-weight: 700; color: #CC6678; margin-bottom: 16px;
    }
    .footer-col a {
      display: block; font-size: 0.87rem; color: rgba(245,240,232,0.55);
      margin-bottom: 10px; text-decoration: none; transition: color 0.2s;
    }
    .footer-col a:hover { color: #FAF6F0; }
    .footer-bottom {
      background: #1A120E; border-top: 1px solid rgba(245,240,232,0.07);
      padding: 20px 60px; display: flex; justify-content: space-between;
      font-size: 0.77rem; color: rgba(245,240,232,0.28);
    }

    /* ── RESPONSIVE ──────────────────────────────── */
    @media (max-width: 960px) {
      .nav { padding: 0 24px; }
      .nav-links .hide-mobile { display: none; }
      section { padding: 64px 24px; }
      .stats-bar { grid-template-columns: repeat(2, 1fr); padding: 36px 24px; gap: 24px; }
      .stat-item { border-right: none; border-bottom: 1px solid #EAE0D8; padding-bottom: 20px; }
      .stat-item:nth-child(2), .stat-item:last-child { border-bottom: none; }
      .products-grid, .testimonials-grid { grid-template-columns: 1fr 1fr; }
      .how-grid { grid-template-columns: 1fr; gap: 40px; }
      .products-header { flex-direction: column; align-items: flex-start; gap: 22px; }
      footer { grid-template-columns: 1fr 1fr; gap: 32px; padding: 40px 24px; }
      .footer-bottom { padding: 16px 24px; flex-direction: column; gap: 6px; }
      .cta-section { padding: 64px 24px; }
    }
    @media (max-width: 580px) {
      .products-grid, .testimonials-grid { grid-template-columns: 1fr; }
      .hero h1 { font-size: 2.5rem; }
      footer { grid-template-columns: 1fr; }
    }
  `],
  templateUrl: './home.component.html'
})
export class HomeComponent { }
